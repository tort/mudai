package com.tort.mudai.person

import akka.actor._
import com.tort.mudai.mapper._
import com.tort.mudai.command.SimpleCommand
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.{KillEvent, StatusLineEvent}
import scala.Some
import akka.actor.Terminated
import scalaz._
import Scalaz._
import java.util
import scala.collection.immutable.SortedMap

class Roamer(val mapper: ActorRef, val pathHelper: PathHelper, val persister: LocationPersister, val person: ActorRef)
  extends QuestHelper
  with ReachabilityHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  import persister._

  def receive = roam

  private def roam: Receive = {
    case RoamZone(zoneName) =>
      loadZoneByName(zoneName) match {
        case None =>
          println(s"### ZONE $zoneName NOT FOUND")
        case Some(zone) =>
          person ! RequestPulses
          println("### ROAMING STARTED")

          val mobsOfZone: Set[Mob] = persister.killableMobsBy(zone)
          val grouped = SortedMap[Int, Set[Mob]]() ++ mobsOfZone.groupBy(_.priority)
          iterateZone(grouped, zoneArea(zone))
      }

    case RoamMobsInArea(targets: Set[Mob], area: Set[Location]) =>
      person ! RequestPulses
      search(targets, area)(singleSearchWaitTarget)

    case KillMobRequest(mob) =>
      person ! RequestPulses
      search(Set(mob), persister.locationByMob(mob.fullName))(singleSearchWaitTarget)
  }

  private def zoneArea(zone: Zone): Set[Location] = {
    reachableFrom(entrance(zone), nonBorderNonLockableNeighbors, locationsOfSummoners(zone)).map(x => persister.loadLocation(x))
  }

  private def singleSearchWaitTarget(searcher: ActorRef): Receive = finishRoamOnFinishSearch andThen waitTarget(searcher)

  private def iteratedSearchWaitTarget(mbs: SortedMap[Int, Set[Mob]], area: Set[Location])(searcher: ActorRef): Receive = roamNextOnFinishSearch(mbs, area) andThen waitTarget(searcher)

  private def iterateZone(mbs: SortedMap[Int, Set[Mob]], area: Set[Location]) {
    mbs.headOption match {
      case None => finishRoaming()
      case Some((priority, mobs)) =>
        search(mobs, area)(iteratedSearchWaitTarget(mbs.tail, area))
    }
  }

  private def roamNextOnFinishSearch(mobs: SortedMap[Int, Set[Mob]], area: Set[Location]): Receive = {
    case SearchFinished =>
    iterateZone(mobs, area)
  }

  private def finishRoamOnFinishSearch: Receive = {
    case SearchFinished =>
      finishRoaming()
  }

  private def waitTarget(searcher: ActorRef): Receive = {
    case MobFound(targets, visibles) =>
      if (!moreThanTwoSameAssistsPresent(visibles)) {
        visibles.find(_.isAgressive) match {
          case Some(m) =>
            attack(m, searcher)
          case None =>
            val assisters = visibles.filter(_.isAssisting).groupBy(x => x).toSeq.sortBy(x => x._2.size).reverse.filter(m => targets.contains(m._1))
            assisters.headOption match {
              case Some((m, xs)) if m.alias.isDefined =>
                attack(m, searcher, xs.size.some)
              case None =>
                targets.headOption.foreach {
                  case m =>
                    attack(m, searcher)
                }
            }
        }
      }
    case SearchFinished =>
      finishRoaming()
    case e => searcher ! e
  }

  private def attack(m: Mob, searcher: ActorRef, number: Option[Int] = None) {
    person ! Attack(m, number)
    become(waitKill(searcher, 0, isSitting = false, isFinished = false, new util.Date))
  }

  protected def moreThanTwoSameAssistsPresent(visibles: Seq[Mob]): Boolean =
    visibles.filter(_.isAssisting).groupBy(_.id).map(x => x._1 -> x._2.size).exists(x => x._2 > 2)

  private def finishRoaming() {
    person ! YieldPulses
    person ! RoamingFinished
    become(roam)
  }

  private def waitKill(searcher: ActorRef, mem: Int, isSitting: Boolean, isFinished: Boolean, attackTime: util.Date): Receive = {
    case KillEvent(_, _, _, magic) =>
      if (magic) {
        person ! new SimpleCommand("взять все")
      } else {
        person ! new SimpleCommand("взять все труп")
      }
    case InterruptRoaming =>
      watch(searcher)
      searcher ! PoisonPill
      become {
        case Terminated(ref) if ref == searcher =>
          finishRoaming()
      }
    case Pulse =>
      if (mem > 0) {
        if (!isSitting) {
          person ! new SimpleCommand("отд")
          become(waitKill(searcher, mem, isSitting = true, isFinished, attackTime))
        }
      } else {
        if (isSitting)
          person ! new SimpleCommand("вст")

        if ((new util.Date().getTime.longValue - attackTime.getTime.longValue) > 5000) {
          become(singleSearchWaitTarget(searcher))
          person ! new SimpleCommand("смотр")
          if (isFinished)
            finishRoaming()
        }
      }
    case StatusLineEvent(_, _, _, m, _, _) =>
      become(waitKill(searcher, m, isSitting, isFinished, attackTime))
    case SearchFinished =>
      become(waitKill(searcher, mem, isSitting, isFinished = true, attackTime))
    case e => searcher ! e
  }
}

case object RoamingFinished

case object InterruptRoaming

case class KillMobRequest(mob: Mob)
