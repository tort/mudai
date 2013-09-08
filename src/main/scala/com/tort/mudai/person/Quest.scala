package com.tort.mudai.person

import akka.actor.{Terminated, Props, Actor, ActorRef}
import com.tort.mudai.mapper.{Location, LocationPersister, PathHelper}
import com.tort.mudai.task.TravelTo
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event.{PeaceStatusEvent, GlanceEvent}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class Quest(mapper: ActorRef, pathHelper: PathHelper, persister: LocationPersister) extends Actor {
  implicit val timeout = Timeout(5 seconds)

  import context._

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      println("### QUEST STARTED")
      val person = sender
      person ! RequestPulses
      person ! new SimpleCommand("см")

      val future = for {
        f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
      } yield f

      future onSuccess {
        case Some(current) =>
          val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister))
          watch(travelTask)
          travelTask ! GoTo(targetLocation)

          become(onArrived(person, travelTask, current))
        case None =>
          println("### CURRENT LOCATION UNDEFINED")
          person ! YieldPulses
          stop(self)
      }
  }

  def onArrived(person: ActorRef, travelTask: ActorRef, startLocation: Location): Receive = {
    case Terminated(task) if (task == travelTask) =>
      person ! new SimpleCommand("см")
      become(onGlance(person, startLocation))
    case command: SimpleCommand => person ! command
    case e => travelTask ! e
  }

  def onFinishFight(person: ActorRef, startLocation: Location): Receive = {
    case PeaceStatusEvent() =>
      person ! new SimpleCommand("взять все все.труп")
      person ! new SimpleCommand("взять все.труп")
      val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister))
      watch(travelTask)
      travelTask ! GoTo(hunterLocation)
      become(onArriveToHunter(person, travelTask, startLocation))
  }

  def onArriveToHunter(person: ActorRef, travelTask: ActorRef, startLocation: Location): Receive = {
    case Terminated(task) if (task == travelTask) =>
      person ! new SimpleCommand("дать труп охот")
      person ! new SimpleCommand("дать труп охот")
      val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister))
      watch(travelTask)
      travelTask ! GoTo(startLocation)
      become(onArrivedToStartLocation(person, travelTask))
    case command: SimpleCommand => person ! command
    case e => travelTask ! e
  }

  def onArrivedToStartLocation(person: ActorRef, travelTask: ActorRef): Receive = {
    case Terminated(task) if (task == travelTask) =>
      person ! new SimpleCommand("постой")
      person ! new SimpleCommand("0")
      person ! YieldPulses
      stop(self)
    case command: SimpleCommand => person ! command
    case e => travelTask ! e
  }

  def onGlance(person: ActorRef, startLocation: Location): Receive = {
    case GlanceEvent(room, None) =>
      room.mobs.exists(_ == "Бобер строит здесь запруду.") match {
        case true =>
          person ! Attack("бобер")
          person ! Attack("2.бобер")
          become(onFinishFight(person, startLocation))
        case false =>
          println("### NO TARGET FOUND")
          val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister))
          watch(travelTask)
          travelTask ! GoTo(startLocation)
          become(onArrivedToStartLocation(person, travelTask))
      }
  }

  def targetLocation: Location = persister.loadLocation("59f7035b-f219-47a1-b207-3f57478d9173")

  def hunterLocation: Location = persister.loadLocation("71f043b1-c4c8-45aa-8cf9-99e0d104f7bc")
}

case object StartQuest
