package com.tort.mudai.person

import com.tort.mudai.command.{RequestWalkCommand, RenderableCommand, WalkCommand, SimpleCommand}
import scalaz.@@
import com.tort.mudai.mapper.{PathHelper, LocationPersister, Location, Direction}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.PeaceStatusEvent
import akka.actor.{Props, Actor, ActorRef}

class WhiteSpiderPlan(val mapper: ActorRef, val pathHelper: PathHelper, val persister: LocationPersister) extends QuestHelper {
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
          finishQuest(person)
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
      person ! YieldPulses
      finishQuest(person)
    })
  }

  def targetLocation = persister.loadLocation("96944805-db9a-4a1a-a91d-d83c5229510a")

  def chestLocation = persister.loadLocation("4e3c6e58-a985-45a6-8352-543a0f24ca42")
}

class WhiteSpiderQuest(val mapper: ActorRef, val pathHelper: PathHelper, val persister: LocationPersister, person: ActorRef) extends Actor {
  import context._

  val quest = actorOf(Props(classOf[WhiteSpiderPlan], mapper, pathHelper, persister))

  def receive = {
    case TriggeredMoveRequest("На сеновале", direction, "На чердаке") =>
      sender ! new SimpleCommand("приставить лестница")
      sender ! new WalkCommand(direction)

    case RequestPulses => person ! RequestPulses
    case YieldPulses =>
      person ! YieldPulses
    case c: RenderableCommand => person ! c
    case w: RequestWalkCommand if sender != person => person ! w
    case a: Attack if sender == quest => person ! a
    case qf@QuestFinished if sender == quest => person ! qf
    case e => quest ! e
  }
}

case class TriggeredMoveRequest(from: String, direction: String @@ Direction, to: String)
