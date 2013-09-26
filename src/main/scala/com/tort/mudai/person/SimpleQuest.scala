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

  def targetLocation: Location = persister.loadLocation("318951cf-d7f6-4ff7-8095-2f22b9e539e0")

  def hunterLocation: Location = persister.loadLocation("e185e8e7-a510-44d2-8c48-740e3723b2b5")
}

case object StartQuest

trait QuestHelper extends Actor {
  def pathHelper: PathHelper

  def mapper: ActorRef

  def persister: LocationPersister

  def goAndDo(targetLocation: Location, person: ActorRef, toDo: () => Unit) {
    val travelTask = context.actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister, person))
    context.watch(travelTask)
    travelTask ! GoTo(targetLocation)

    context.become(onArrived(person, travelTask, toDo))
  }

  def onArrived(person: ActorRef, travelTask: ActorRef, toDo: () => Unit): Receive = {
    case Terminated(task) if (task == travelTask) => toDo()
    case command: SimpleCommand => person ! command
    case e => travelTask forward e
  }

  def finishQuest {
    println("QUEST FINISHED")
  }
}
