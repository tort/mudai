package com.tort.mudai.task

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.person.{CurrentLocation, Pulse}
import com.tort.mudai.mapper._
import com.tort.mudai.command.SimpleCommand
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scalaz._
import Scalaz._
import com.tort.mudai.person.GoTo
import com.tort.mudai.command.RequestWalkCommand
import com.tort.mudai.event.DiscoverObstacleEvent
import scala.Some
import com.tort.mudai.person.RawRead
import Direction._

class TravelTo(pathHelper: PathHelper, mapper: ActorRef, locationPersister: LocationPersister, person: ActorRef) extends Actor {
  implicit val timeout = Timeout(5 seconds)

  import context._

  def receive: Receive = goto

  def goto: Receive = {
    case GoTo(target) =>
      val future = for {
        f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
      } yield f

      future onSuccess {
        case current =>
          val path = pathHelper.pathTo(current, target)
          current match {
            case None =>
              sender ! RawRead("### CURRENT LOCATION UNDEFINED")
              context.stop(self)
            case Some(cur) =>
              become(pulse(path.some, cur, target))
              println("WAIT FOR PULSES")
          }
      }
  }

  def waitMove(path: Option[Seq[String @@ Direction]], current: Location, target: Location): Receive =
    pfWaitMove(path, current, target).orElse(pfMove(path, current, target))

  def pfWaitMove(path: Option[Seq[String @@ Direction]], current: Location, target: Location): Receive = {
    case DiscoverObstacleEvent(obstacle) =>
      person ! new SimpleCommand("открыть %s %s".format(obstacle, path.head.head))
      person ! RequestWalkCommand(path.head.head) //TODO fix
  }

  def pulse(path: Option[Seq[String @@ Direction]], current: Location, target: Location): Receive =
    pfPulse(path, current, target).orElse(pfMove(path, current, target))

  def pfMove(path: Option[Seq[String @@ Direction]], current: Location, target: Location): Receive = {
    case MoveEvent(Some(from), direction, to) =>
      direction match {
        case Some(dir) if dir === path.head.head && from.id === current.id =>
          become(pulse(path.map(_.tail), to, target))
        case _ =>
          become(pulse(None, to, target))
      }
  }

  def pfPulse(path: Option[Seq[String @@ Direction]], current: Location, target: Location): Receive = {
    case Pulse =>
      path match {
        case Some(Nil) =>
          context.stop(self)
        case Some(p) =>
          person ! RequestWalkCommand(p.head)
          become(waitMove(path, current, target))
        case None =>
          pathHelper.pathTo(current.some, target) match {
            case Nil =>
              context.stop(self)
            case newPath =>
              person ! RequestWalkCommand(newPath.head)
              become(waitMove(newPath.some, current, target))
          }
      }
  }
}
