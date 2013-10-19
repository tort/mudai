package com.tort.mudai.quest

import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.person._
import com.tort.mudai.mapper.{Location, PathHelper, LocationPersister, Zone}
import akka.actor.ActorRef
import com.tort.mudai.event.KillEvent
import com.tort.mudai.person.Attack
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
            case l =>
              goAndDo(persister.locationByMob("Старик-отшельник отрешенно смотрит сквозь Вас.").head, person, (l) => {
                system.scheduler.scheduleOnce(5 second, self, FiveSeconds)
                become(waitTime)
              })
          }
      }
  }

  private def waitTime: Receive = {
    case FiveSeconds =>
      person ! new SimpleCommand("г помогу")
      person ! Roam(Zone.name("Посыльная дорога - у главы"))
      become(waitRoamFinish)
  }

  private def waitRoamFinish: Receive = {
    case RoamingFinished =>
      goAndDo(persister.locationByMob("Главарь разбойников стоит здесь.").head, person, (l) => {
        person ! Attack("главарь")
        become(waitKill)
      })
  }

  private def waitKill: Receive = {
    case KillEvent(_, _, _, _) =>
      person ! new SimpleCommand("взять труп")
      goAndDo(persister.locationByMob("Старик-отшельник отрешенно смотрит сквозь Вас.").head, person, (l) => {
        person ! new SimpleCommand("сн шап")
        person ! new SimpleCommand("дать труп стар")
        person ! new SimpleCommand("одеть шап")
        finishQuest(person)
      })
  }
}

case object FiveSeconds
