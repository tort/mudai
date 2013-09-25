package com.tort.mudai.person

import com.tort.mudai.command.{WalkCommand, SimpleCommand}
import scalaz.@@
import com.tort.mudai.mapper.{Location, Direction}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.PeaceStatusEvent
import akka.actor.ActorRef

class WhiteSpiderQuest extends QuestHelper {
  implicit val timeout = Timeout(5 seconds)

  import context._

  def receive = {
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

    case TriggeredMoveRequest("На сеновале", direction, "На чердаке") =>
      sender ! new SimpleCommand("приставить лестница")
      sender ! new WalkCommand(direction)
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
}

case class TriggeredMoveRequest(from: String, direction: String @@ Direction, to: String)
