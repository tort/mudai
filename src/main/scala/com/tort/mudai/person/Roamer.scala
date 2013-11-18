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

  private val ThreeSeconds: Int = 3000

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
          iterateZone(grouped, zone)
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

  private def singleSearchWaitTarget(searcher: ActorRef, isFinished: Boolean): Receive =
    finishRoamOnFinishSearch(searcher) orElse waitTarget(searcher, singleSearchWaitTarget, isFinished)

  private def iteratedSearchWaitTarget(mbs: SortedMap[Int, Set[Mob]], zone: Zone)(searcher: ActorRef, isFinished: Boolean): Receive =
    roamNextOnFinishSearch(mbs, zone) orElse waitTarget(searcher, iteratedSearchWaitTarget(mbs, zone), isFinished)

  private def iterateZone(mbs: SortedMap[Int, Set[Mob]], zone: Zone) {
    mbs.headOption match {
      case None => finishRoaming()
      case Some((priority, mobs)) =>
        println(s" ### ITERATE: $priority")
        mobs.foreach(m => println(m.shortName))
        val area = mobs.forall(m => persister.locationByMob(m.fullName).size == 1) match {
          case true =>
            mobs.flatMap(m => persister.locationByMob(m.fullName))
          case false =>
            zoneArea(zone)
        }
        search(mobs, area)(iteratedSearchWaitTarget(mbs.tail, zone))
    }
  }

  private def roamNextOnFinishSearch(mobs: SortedMap[Int, Set[Mob]], zone: Zone): Receive = {
    case SearchFinished =>
      iterateZone(mobs, zone)
  }

  private def finishRoamOnFinishSearch(searcher: ActorRef): Receive = {
    case SearchFinished =>
      become(waitKill(searcher, 0, false, isFinished = true, new util.Date(), singleSearchWaitTarget))
  }

  private def waitTarget(searcher: ActorRef, specificWaitTarget: (ActorRef, Boolean) => Receive, isFinished: Boolean): Receive = {
    case NoTargetsFound =>
      if(isFinished)
        finishRoaming()
    case MobFound(targets, visibles) =>
      if (!moreThanTwoSameAssistsPresent(visibles)) {
        visibles.find(_.isAgressive) match {
          case Some(m) =>
            attack(m, searcher, specificWaitTarget)
          case None =>
            val assisters = visibles.filter(_.isAssisting).groupBy(x => x).toSeq.sortBy(x => x._2.size).reverse.filter(m => targets.contains(m._1))
            assisters.headOption match {
              case Some((m, xs)) if m.alias.isDefined =>
                attack(m, searcher, specificWaitTarget, xs.size.some)
              case None =>
                targets.headOption.foreach(attack(_, searcher, specificWaitTarget))
            }
        }
      }
    case e => searcher ! e
  }

  private def attack(m: Mob, searcher: ActorRef, specificWaitTarget: (ActorRef, Boolean) => Receive, number: Option[Int] = None) {
    person ! Attack(m, number)
    become(waitKill(searcher, 0, isSitting = false, isFinished = false, new util.Date, specificWaitTarget))
  }

  protected def moreThanTwoSameAssistsPresent(visibles: Seq[Mob]): Boolean =
    visibles.filter(_.isAssisting).groupBy(_.id).map(x => x._1 -> x._2.size).exists(x => x._2 > 2)

  private def finishRoaming() {
    println("### ROAMING FINISHED")
    person ! YieldPulses
    person ! RoamingFinished
    become(roam)
  }

  private def waitKill(searcher: ActorRef, mem: Int, isSitting: Boolean, isFinished: Boolean, attackTime: util.Date, specificWaitTarget: (ActorRef, Boolean) => Receive): Receive = {
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
          become(waitKill(searcher, mem, isSitting = true, isFinished, attackTime, specificWaitTarget))
        }
      } else {
        if (isSitting)
          person ! new SimpleCommand("вст")

        if ((new util.Date().getTime.longValue - attackTime.getTime.longValue) > ThreeSeconds) {
          become(specificWaitTarget(searcher, isFinished))
          person ! new SimpleCommand("смотр")
        }
      }
    case StatusLineEvent(_, _, _, m, _, _) =>
      become(waitKill(searcher, m, isSitting, isFinished, attackTime, specificWaitTarget))
    case SearchFinished =>
      become(waitKill(searcher, mem, isSitting, isFinished = true, attackTime, specificWaitTarget))
    case e => searcher ! e
  }
}

case object RoamingFinished

case object InterruptRoaming

case class KillMobRequest(mob: Mob)
