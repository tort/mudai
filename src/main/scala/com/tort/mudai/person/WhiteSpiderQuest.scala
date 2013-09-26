package com.tort.mudai.person

import com.tort.mudai.command.{RequestWalkCommand, RenderableCommand, WalkCommand, SimpleCommand}
import scalaz.@@
import com.tort.mudai.mapper.{PathHelper, LocationPersister, Location, Direction}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.PeaceStatusEvent
import akka.actor.{Props, Actor, ActorRef}

class WhiteSpiderQuest(val mapper: ActorRef, val pathHelper: PathHelper, val persister: LocationPersister) extends QuestHelper {
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
            person ! Attack("паук")
            become(onFinishFight(person, current))
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
      goAndDo(chestLocation, person, () => {
        person ! new SimpleCommand("отпер сунд")
        person ! new SimpleCommand("откр сунд")
        person ! new SimpleCommand("взять все сунд")
        person ! new SimpleCommand("взять все")
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

  def targetLocation = persister.loadLocation("a3929190-8baf-4f4d-a13c-4452b61df853")

  def chestLocation = persister.loadLocation("12544efe-858d-49c2-b084-21b5025a9cbb")
}

class WhiteSpiderAgg(val mapper: ActorRef, val pathHelper: PathHelper, val persister: LocationPersister, person: ActorRef) extends Actor {
  import context._

  val quest = actorOf(Props(classOf[WhiteSpiderQuest], mapper, pathHelper, persister))

  def receive = {
    case TriggeredMoveRequest("На сеновале", direction, "На чердаке") =>
      sender ! new SimpleCommand("приставить лестница")
      sender ! new WalkCommand(direction)

    case RequestPulses => person ! RequestPulses
    case YieldPulses => person ! YieldPulses
    case c: RenderableCommand => person ! c
    case w: RequestWalkCommand => person ! w
    case a: Attack => person ! a
    case e => quest ! e
  }
}

case class TriggeredMoveRequest(from: String, direction: String @@ Direction, to: String)
