package com.tort.mudai.person

import akka.actor.{Terminated, Props, Actor, ActorRef}
import com.tort.mudai.mapper.{Location, LocationPersister, PathHelper}
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.task.TravelTo
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.GlanceEvent

class Roamer(mapper: ActorRef, pathHelper: PathHelper, persister: LocationPersister) extends Actor {
  import context._
  implicit val timeout = Timeout(5 seconds)

  import persister._

  def receive = roam

  def roam: Receive = {
    case Roam(zoneName) =>
      loadZoneByName(zoneName).foreach {
        case zone =>
          val person = sender
          println("ROAMING STARTED")

          val future = for {
            f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
          } yield f

          future onSuccess {
            case current =>
              current.foreach(l => become(visit(person, killablesHabitation(zone) :+ l)))
          }
      }
  }

  def visit(person: ActorRef, locations: Seq[Location]): Receive = locations match {
    case Nil =>
      println("ROAMING FINISHED")
      person ! RoamingFinished
      roam
    case x :: xs =>
      println("VISIT " + x.title)
      val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister))
      watch(travelTask)
      travelTask ! GoTo(x)

      base(person, travelTask, xs)
  }

  private def waitReadyForFight(person: ActorRef, travelTask: ActorRef, xs: Seq[Location]): Receive = {
    case ReadyForFight =>
      become(base(person, travelTask, xs))
      person ! new SimpleCommand("вст")
  }

  private def base(person: ActorRef, travelTask: ActorRef, xs: Seq[Location]): Receive = {
    case Terminated(ref) if ref == travelTask =>
      become(visit(person, xs))
      println("### TRAVEL SUBTASK TERMINATED")
    case NeedMem =>
      person ! new SimpleCommand("отд")
      become(waitReadyForFight(person, travelTask, xs))
    case e@GlanceEvent(room, direction) =>
      room.mobs.flatMap(mobByFullName(_)).filter(_.killable).headOption.foreach {
        case mob =>
          mob.alias.foreach {
            case x =>
              person ! Attack(x)
          }
      }
      travelTask ! e
    case InterruptRoaming =>
      become(visit(person, xs.last :: Nil))
    case c: RenderableCommand => person ! c
    case e => travelTask ! e

  }
}

case object RoamingFinished
case object InterruptRoaming
