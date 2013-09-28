package com.tort.mudai.person

import akka.actor.{Actor, ActorRef}
import com.tort.mudai.mapper.{Location, LocationPersister, PathHelper}
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
          finishQuest(person)
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
      person ! YieldPulses
      finishQuest(person)
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

  def targetLocation: Location = persister.locationByTitle("У запруды").head

  def hunterLocation: Location = persister.locationByTitle("Жилище охотника").head
}

class Passages extends Actor {
  def receive = {
    case TriggeredMoveRequest("У шалаша", direction, "Тихий угол") =>
      sender ! new SimpleCommand("дать 13 кун следопыт")
  }
}
