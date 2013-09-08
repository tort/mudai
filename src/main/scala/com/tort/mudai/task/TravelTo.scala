package com.tort.mudai.task

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.person.{RawRead, CurrentLocation, Pulse, GoTo}
import com.tort.mudai.mapper.{LocationPersister, Location, Direction, PathHelper}
import com.tort.mudai.command.SimpleCommand
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.{DiscoverObstacleEvent, GlanceEvent}

class TravelTo(pathHelper: PathHelper, mapper: ActorRef, locationPersister: LocationPersister) extends Actor {
  implicit val timeout = Timeout(5 seconds)

  import context._

  def receive: Receive = goto

  def goto: Receive = {
    case GoTo(target) =>
      val person = sender

      val future = for {
        f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
      } yield f

      future onSuccess {
        case current =>
          val path = pathHelper.pathTo(current, target)
          current match {
            case None =>
              person ! RawRead("### CURRENT LOCATION UNDEFINED")
              context.stop(self)
            case Some(cur) =>
              become(pulse(person, path, cur, target))
              println("WAIT FOR PULSES")
          }
      }
  }

  def waitMove(person: ActorRef, path: Seq[Direction], previous: Location, target: Location): Receive = {
    case GlanceEvent(room, Some(direction)) =>
      val shouldBeNewCurrent: Location = locationPersister.loadLocation(previous, direction).get //locations from path => must exist
      if (shouldBeNewCurrent.title == room.title && shouldBeNewCurrent.desc == room.desc)
        become(pulse(person, path.tail, shouldBeNewCurrent, target))
      else {
        val future = for {
          f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
        } yield f

        future onSuccess {
          case current =>
            val path = pathHelper.pathTo(current, target)
            current match {
              case None =>
                person ! RawRead("### CURRENT LOCATION UNDEFINED")
                context.stop(self)
              case Some(cur) => become(pulse(person, path, cur, target))
            }
        }
      }
    case DiscoverObstacleEvent(obstacle) =>
      person ! new SimpleCommand("открыть %s %s".format(obstacle, path.head.id))
      person ! new SimpleCommand(path.head.id)
  }

  def pulse(person: ActorRef, path: Seq[Direction], current: Location, target: Location): Receive = {
    case GlanceEvent(room, direction) =>
      val future = for {
        f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
      } yield f

      future onSuccess {
        case current =>
          val path = pathHelper.pathTo(current, target)
          current match {
            case None =>
              person ! RawRead("### CURRENT LOCATION UNDEFINED")
              context.stop(self)
            case Some(cur) =>
              become(pulse(person, path, cur, target))

          }
      }
    case Pulse =>
      path match {
        case Nil =>
          context.stop(self)
        case p =>
          person ! new SimpleCommand(p.head.id)
          become(waitMove(person, p, current, target))
      }
  }
}
