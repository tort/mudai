package com.tort.mudai.task

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.person.{CurrentLocation, Pulse, GoTo}
import com.tort.mudai.mapper.{Location, Direction, PathHelper}
import com.tort.mudai.command.SimpleCommand
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.{DiscoverObstacleEvent, GlanceEvent}

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

  def waitMove(person: ActorRef, path: Seq[Direction]): Receive = {
    case GlanceEvent(room, Some(direction)) =>
      become(pulse(person, path.tail))
    case DiscoverObstacleEvent(obstacle) =>
      person ! new SimpleCommand("открыть %s %s".format(obstacle, path.head.id))
      person ! new SimpleCommand(path.head.id)
  }

  def pulse(person: ActorRef, path: Seq[Direction]): Receive = {
    case Pulse =>
      path match {
        case Nil =>
          context.stop(self)
        case p =>
          person ! new SimpleCommand(p.head.id)
          become(waitMove(person, p))
      }
  }
}
