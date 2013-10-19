package com.tort.mudai.quest

import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.person._
import com.tort.mudai.mapper.{Location, PathHelper, LocationPersister, Zone}
import akka.actor.{Cancellable, ActorRef}
import com.tort.mudai.event.KillEvent
import com.tort.mudai.person.Roam
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class MainRogueQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      println("QUEST STARTED")
      person ! RequestPulses

      val future = for {
        f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
      } yield f

      future onSuccess {
        case current =>
          current.foreach {
            case startLocation =>
              goAndDo(persister.locationByMob("Старик-отшельник отрешенно смотрит сквозь Вас.").head, person, (l) => {
                val future = system.scheduler.scheduleOnce(5 second, self, FiveSeconds)
                person ! new SimpleCommand("г помогу")
                become(waitTime(startLocation, future))
              })
          }
      }
  }

  def interruptQuest(startLocation: Location) {
    goAndDo(startLocation, person, (l) => {
      finishQuest(person)
      become(quest)
    })
  }

  private def waitTime(startLocation: Location, future: Cancellable): Receive = {
    case FiveSeconds =>
      println("### QUEST NOT RESPONDING")
      interruptQuest(startLocation)
    case RawRead(text) if text.matches( """(?ms).*Ступай, сынок, очисти святое место от этого недостойного человека\.\..*""") =>
      person ! Roam(Zone.name("Посыльная дорога - у главы"))
      future.cancel()
      become(waitRoamFinish)
  }

  private def waitRoamFinish: Receive = {
    case RoamingFinished =>
      person ! KillMobRequest(persister.mobByFullName("Главарь разбойников стоит здесь.").get)
      become(waitKill)
  }

  private def waitKill: Receive = {
    case KillEvent(shortName, _, _, _) if shortName == "Главарь разбойников" =>
      person ! new SimpleCommand("взять глав")
      goAndDo(persister.locationByMob("Старик-отшельник отрешенно смотрит сквозь Вас.").head, person, (l) => {
        person ! new SimpleCommand("сн шап")
        person ! new SimpleCommand("дать труп стар")
        person ! new SimpleCommand("одеть шап")
        finishQuest(person)
      })
  }
}

case object FiveSeconds
