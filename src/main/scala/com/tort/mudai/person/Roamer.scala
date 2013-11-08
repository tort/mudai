package com.tort.mudai.person

import akka.actor._
import com.tort.mudai.mapper._
import com.tort.mudai.command.SimpleCommand
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.{GlanceEvent, KillEvent, StatusLineEvent}
import scala.Some
import akka.actor.Terminated
import Mob._
import scalaz._
import Scalaz._

class Roamer(val mapper: ActorRef, val pathHelper: PathHelper, val persister: LocationPersister, val person: ActorRef)
  extends QuestHelper
  with ReachabilityHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  import persister._

  def receive = roam

  def roam: Receive = {
    case Roam(zoneName) =>
      loadZoneByName(zoneName) match {
        case None =>
          println(s"### ZONE $zoneName NOT FOUND")
        case Some(zone) =>
          person ! RequestPulses
          println("### ROAMING STARTED")

          search(persister.killableMobsBy(zone), area(zone))(waitTarget)
      }

    case RoamArea(targets: Set[Mob], area: Set[Location]) =>
      person ! RequestPulses
      search(targets, area)(waitTarget)

    case KillMobRequest(mob) =>
      person ! RequestPulses
      search(Set(mob), persister.locationByMob(mob.fullName))(waitTarget)
  }

  private def area(zone: Zone): Set[Location] = {
    reachableFrom(entrance(zone), nonBorderNonLockableNeighbors, locationsOfSummoners(zone)).map(x => persister.loadLocation(x))
  }

  def waitTarget(searcher: ActorRef): Receive = {
    case MobFound(targets, visibles) =>
      if (!moreThanTwoSameAssistsPresent(visibles)) {
        visibles.filter(_.isAgressive).headOption match {
          case Some(m) =>
            person ! Attack(m)
            become(waitKill(searcher, 0, isSitting = false, isFinished = false))
          case None =>
            val assisters = visibles.filter(_.isAssisting).groupBy(x => x).toSeq.sortBy(x => x._2.size).reverse.filter(m => targets.contains(m._1))
            assisters.headOption match {
              case Some((m, xs)) if m.alias.isDefined =>
                person ! Attack(m, xs.size.some)
                become(waitKill(searcher, 0, isSitting = false, isFinished = false))
              case None =>
                targets.headOption.foreach {
                  case m => person ! Attack(m)
                }
                become(waitKill(searcher, 0, isSitting = false, isFinished = false))
            }
        }
      }
    case SearchFinished =>
      finishRoaming()
    case e => searcher ! e
  }


  protected def moreThanTwoSameAssistsPresent(visibles: Seq[Mob]): Boolean =
    visibles.filter(_.isAssisting).groupBy(_.id).map(x => x._1 -> x._2.size).exists(x => x._2 > 2)

  private def finishRoaming() {
    person ! YieldPulses
    person ! RoamingFinished
    become(roam)
  }

  private def waitKill(searcher: ActorRef, mem: Int, isSitting: Boolean, isFinished: Boolean): Receive = {
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
          become(waitKill(searcher, mem, isSitting = true, isFinished))
        }
      } else {
        person ! new SimpleCommand("вст")
        become(waitTarget(searcher))
        person ! new SimpleCommand("смотр")
        if (isFinished)
          finishRoaming()
      }
    case StatusLineEvent(_, _, _, m, _, _) =>
      become(waitKill(searcher, m, isSitting, isFinished))
    case SearchFinished =>
      become(waitKill(searcher, mem, isSitting, isFinished = true))
    case e => searcher ! e
  }
}

case object RoamingFinished

case object InterruptRoaming

case class KillMobRequest(mob: Mob)
