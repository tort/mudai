package com.tort.mudai.person

import akka.actor._
import com.tort.mudai.mapper.{Location, LocationPersister, PathHelper}
import com.tort.mudai.command.SimpleCommand
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.Terminated
import com.tort.mudai.event.{StatusLineEvent, KillEvent}
import scala.Some

class Roamer(val mapper: ActorRef, val pathHelper: PathHelper, val persister: LocationPersister, val person: ActorRef) extends QuestHelper {

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
              search(persister.killableMobsBy(zone))(waitTarget(current))
            case None =>
              println("### CURRENT LOCATION UNKNOWN")
          }
      }
  }

  def waitTarget(current: Location)(searcher: ActorRef): Receive = {
    case MobFound(alias) =>
      person ! Attack(alias)
      become(waitKill(searcher, current, 0, isSitting = false))
    case SearchFinished =>
      finishRoaming(current)
    case e => searcher ! e
  }

  private def finishRoaming(current: Location) {
    goAndDo(current, person, (visited) => {
      person ! YieldPulses
      person ! RoamingFinished
      become(roam)
      println("### TRAVEL SUBTASK TERMINATED")
    })
  }

  private def waitKill(searcher: ActorRef, current: Location, mem: Int, isSitting: Boolean): Receive = {
    case KillEvent(_, _, _) =>
      person ! new SimpleCommand("взять все труп")
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
