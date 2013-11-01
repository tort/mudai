package com.tort.mudai.person

import akka.pattern.ask
import akka.actor._
import scala.concurrent.duration._
import akka.util.Timeout
import com.tort.mudai.mapper.Direction
import scalaz._
import com.tort.mudai.event.{FleeEvent, FightRoundEvent}
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}

class Fleeker(mapper: ActorRef) extends Actor {

  import context._

  implicit val timeout = Timeout(5 seconds)

  import Direction._

  def receive = rec

  def rec: Receive = {
    case FightRoundEvent(_, target, _) =>
      sender ! Assist
      for {
        dirOpt <- (mapper ? LastDirection).mapTo[Option[String @@ Direction]]
      } yield {
        mapper ! PreMoveHint
        become(waitFlee(dirOpt.get))
        flee(dirOpt.get)
      }
  }

  def flee(direction: String @@ Direction) {
    sender ! FleeCommand(oppositeDirection(direction))
  }

  def waitFlee(direction: String @@ Direction): Receive = {
    case FleeEvent() =>
      context.become(rec)
      sender ! new SimpleCommand(s"$direction")
  }
}

case object LastDirection

case object Assist

case object PreMoveHint

case class FleeCommand(direction: String @@ Direction) extends RenderableCommand {
  def render = s"беж ${direction}"
}
