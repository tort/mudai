package com.tort.mudai.task

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.person.{CurrentLocation, Pulse, GoTo}
import com.tort.mudai.mapper.{Location, Direction, PathHelper}
import com.tort.mudai.command.SimpleCommand
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class TravelTo(pathHelper: PathHelper, mapper: ActorRef) extends Actor {
  implicit val timeout = Timeout(5 seconds)
  import context._

  def receive: Receive = goto

  def goto: Receive = {
    case GoTo(loc) =>
      val person = sender

      val future = for {
        f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
      } yield f

      future onSuccess {
        case current =>
          val path = pathHelper.pathTo(current, loc)
          become(pulse(person, path))
      }
  }

  def pulse(person: ActorRef, path: Seq[Direction]): Receive = {
    case Pulse =>
      path match {
        case Nil =>
          person ! TravelFinished
          context.stop(self)
        case p =>
          person ! new SimpleCommand(p.head.id)
          become(pulse(person, p.tail))
      }
  }
}

case object TravelFinished
