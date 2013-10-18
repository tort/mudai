package com.tort.mudai.person

import akka.actor._
import com.tort.mudai.mapper._
import com.tort.mudai.command.SimpleCommand
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.KillEvent
import scala.Some
import akka.actor.Terminated
import com.tort.mudai.event.StatusLineEvent

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
          val person = sender
          person ! RequestPulses
          println("ROAMING STARTED")

          val future = for {
            f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
          } yield f

          future onSuccess {
            case Some(current) =>
              search(persister.killableMobsBy(zone), area(zone))(waitTarget(current))
            case None =>
              println("### CURRENT LOCATION UNKNOWN")
          }
      }
  }

  private def area(zone: Zone): Set[Location] = {
    reachableFrom(entrance(zone), nonBorderNonLockableNeighbors, locationsOfSummoners(zone)).map(x => persister.loadLocation(x))
  }

  def waitTarget(current: Location)(searcher: ActorRef): Receive = {
    case MobFound(targets, visibles) =>
      if (!moreThanTwoSameAssistsPresent(visibles)) {
        val group = visibles.filter(_.isAssisting).groupBy(x => x).toSeq.sortBy(x => x._2.size).reverse.filter(m => targets.contains(m._1))
        group.headOption match {
          case Some((m, xs)) if (m.alias.isDefined) =>
            person ! Attack(s"${xs.size}.${m.alias.get}")
            become(waitKill(searcher, current, 0, isSitting = false))
          case None =>
            targets.headOption.foreach(m => person ! Attack(m.alias.get))
            become(waitKill(searcher, current, 0, isSitting = false))
        }
      }
    case SearchFinished =>
      finishRoaming(current)
    case e => searcher ! e
  }


  protected def moreThanTwoSameAssistsPresent(visibles: Seq[Mob]): Boolean =
    visibles.filter(_.isAssisting).groupBy(_.id).map(x => x._1 -> x._2.size).find(x => x._2 > 2).isDefined

  private def finishRoaming(current: Location) {
    goAndDo(current, person, (visited) => {
      person ! YieldPulses
      person ! RoamingFinished
      become(roam)
      println("### TRAVEL SUBTASK TERMINATED")
    })
  }

  private def waitKill(searcher: ActorRef, current: Location, mem: Int, isSitting: Boolean): Receive = {
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
          finishRoaming(current)
      }
    case Pulse =>
      if (mem > 0) {
        if (!isSitting) {
          person ! new SimpleCommand("отд")
          become(waitKill(searcher, current, mem, isSitting = true))
        }
      } else {
        person ! new SimpleCommand("вст")
        become(waitTarget(current)(searcher))
      }
    case StatusLineEvent(_, _, _, mem, _, _) =>
      become(waitKill(searcher, current, mem, isSitting))
    case e => searcher ! e
  }
}

case object RoamingFinished

case object InterruptRoaming
