package com.tort.mudai.person

import akka.actor.Actor
import com.tort.mudai.mapper.{MoveEvent, Direction, Location}
import scalaz._
import Scalaz._
import com.tort.mudai.event.{FleeEvent, KillEvent, FightRoundEvent, GlanceEvent}
import com.tort.mudai.command.SimpleCommand

class Fleeker extends Actor {
  import Direction._

  def receive = rec(None, None, None)

  def rec(from: Option[Location], direction: Option[String @@ Direction], to: Option[Location]): Receive = {
    case GlanceEvent(_, _) =>
      context.become(rec(None, None, None))
    case FightRoundEvent(_, target, _) =>
      sender ! RequestPulses
      sender ! Assist
      for {
        f <- from
        d <- direction
        t <- to
      } yield {
        flee(f, d, t)
        context.become(waitFlee(f, d, t, target))
      }
    case MoveEvent(f, d, t) =>
      context.become(rec(f, d, t.some))
    case KillEvent(target, _, _, _) =>
      context.become(rec(from, direction, to))
  }

  def flee(from: Location, direction: String @@ Direction, to: Location) {
    sender ! new SimpleCommand(s"беж ${oppositeDirection(direction)}")
  }

  def waitFlee(from: Location, direction: String @@ Direction, to: Location, target: String): Receive = {
    case FightRoundEvent(_, target, _) =>
      flee(from, direction, to)
    case FleeEvent() =>
      context.become(rec(None, None, None))
      sender ! new SimpleCommand(s"$direction")
      sender ! FleeMove(to, oppositeDirection(direction), from)
      sender ! Attack(target)
  }
}

case object Assist
