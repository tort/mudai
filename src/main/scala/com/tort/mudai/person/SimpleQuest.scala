package com.tort.mudai.person

import akka.actor.{Terminated, Props, Actor, ActorRef}
import com.tort.mudai.mapper.{Location, LocationPersister, PathHelper}
import com.tort.mudai.task.TravelTo
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event.{PeaceStatusEvent, GlanceEvent}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class SimpleQuest(val mapper: ActorRef, val pathHelper: PathHelper, val persister: LocationPersister) extends QuestHelper {
  implicit val timeout = Timeout(5 seconds)

  import context._

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      println("### QUEST STARTED")
      val person = sender
      person ! RequestPulses

      val future = for {
        f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
      } yield f

      future onSuccess {
        case Some(current) =>
          goAndDo(targetLocation, person, () => {
            person ! new SimpleCommand("см")
            become(onGlance(person, current))
          })
        case None =>
          println("### CURRENT LOCATION UNDEFINED")
          person ! YieldPulses
          finishQuest
      }
  }

  def onFinishFight(person: ActorRef, startLocation: Location): Receive = {
    case PeaceStatusEvent() =>
      person ! new SimpleCommand("взять все все.труп")
      person ! new SimpleCommand("взять все.труп")
      goAndDo(hunterLocation, person, () => {
        person ! new SimpleCommand("дать труп охот")
        person ! new SimpleCommand("дать труп охот")
        goRentAndFinishQuest(startLocation, person)
      })
  }


  def goRentAndFinishQuest(startLocation: Location, person: ActorRef) {
    goAndDo(startLocation, person, () => {
      person ! new SimpleCommand("постой")
      person ! new SimpleCommand("0")
      person ! YieldPulses
      finishQuest
    })
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
          goRentAndFinishQuest(startLocation, person)
      }
  }

  def targetLocation: Location = persister.loadLocation("4296145c-f360-4d6f-9cab-0a30ebccb223")

  def hunterLocation: Location = persister.loadLocation("f531a8c6-cc57-4546-80a9-c26c04d8398d")
}

case object StartQuest

trait QuestHelper extends Actor {
  def pathHelper: PathHelper

  def mapper: ActorRef

  def persister: LocationPersister

  def goAndDo(targetLocation: Location, person: ActorRef, toDo: () => Unit) {
    val travelTask = context.actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister))
    context.watch(travelTask)
    travelTask ! GoTo(targetLocation)

    context.become(onArrived(person, travelTask, toDo))
  }

  def onArrived(person: ActorRef, travelTask: ActorRef, toDo: () => Unit): Receive = {
    case Terminated(task) if (task == travelTask) =>
      toDo()
    case command: SimpleCommand => person ! command
    case e => travelTask ! e
  }

  def finishQuest {
    println("QUEST FINISHED")
  }
}
