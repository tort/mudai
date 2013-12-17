package com.tort.mudai.person

import akka.pattern.ask
import akka.actor._
import scala.concurrent.duration._
import akka.util.Timeout
import com.tort.mudai.mapper.{LocationPersister, MoveEvent, Direction}
import scalaz._
import com.tort.mudai.event.{PeaceStatusEvent, FleeEvent, FightRoundEvent}
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.quest.TimeOut
import com.tort.mudai.mapper.Mob.ShortName
import com.tort.mudai.person.ThreshholdedFleeker.PeriodExpired

class Fleeker(mapper: ActorRef, persister: LocationPersister) extends Actor {

  import context._

  implicit val timeout = Timeout(5 seconds)

  import Direction._

  def receive = rec

  def rec: Receive = {
    case FightRoundEvent(_, target, _) =>
      val s = sender
      s ! Assist
      for {
        dirOpt <- (mapper ? LastDirection).mapTo[Option[String @@ Direction]]
      } yield {
        flee(dirOpt.get, s)
        become(waitFlee(dirOpt.get, target))
      }
  }

  def flee(direction: String @@ Direction, s: ActorRef) {
    s ! FleeCommand(oppositeDirection(direction))
  }

  def waitFlee(direction: String @@ Direction, target: String @@ ShortName): Receive = {
    case FleeEvent() =>
      become(waitMoveEvent(target))
      sender ! new SimpleCommand(s"$direction")
    case PeaceStatusEvent() =>
      system.scheduler.scheduleOnce(2 seconds, self, TimeOut)
    case TimeOut =>
      become(rec)
  }

  private def waitMoveEvent(target: String @@ ShortName): Receive = {
    case MoveEvent(_, _, _) =>
      become(rec)
      for {
        mob <- persister.mobByShortName(target)
      } yield sender ! Attack(mob, None)
  }
}

case object LastDirection

case object Assist

case class FleeCommand(direction: String @@ Direction) extends RenderableCommand {
  def render = s"беж ${direction}"
}

class ThreshholdedFleeker(mapper: ActorRef, persister: LocationPersister) extends Actor {
  private val fleeker = context.actorOf(Props(classOf[Fleeker], mapper, persister))

  def receive = rec

  private def rec: Receive = {
    case e@FightRoundEvent(_, _, _) =>
      context.become {
        case PeriodExpired =>
          context.unbecome()
      }
      fleeker forward e
  }
}

object ThreshholdedFleeker {
  case object PeriodExpired
}
