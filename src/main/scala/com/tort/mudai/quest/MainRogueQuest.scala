package com.tort.mudai.quest

import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.person._
import com.tort.mudai.mapper._
import akka.actor.{Cancellable, ActorRef}
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.KillMobRequest
import com.tort.mudai.event.KillEvent
import scalaz._
import Scalaz._

class MainRogueQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  def receive = quest

  val questerLocation: Location = persister.locationByMob("Старик-отшельник отрешенно смотрит сквозь Вас.").head
  val mainRogue: Mob = persister.mobByFullName("Главарь разбойников стоит здесь.").get
  val QuestTakenPattern = """(?ms).*Ступай, сынок, очисти святое место от этого недостойного человека\.\..*"""
  val assists: Mob = persister.mobByFullName("Разбойник крадется мимо Вас.").get

  def quest: Receive = {
    case StartQuest =>
      println("QUEST STARTED")
      person ! RequestPulses

      goAndDo(questerLocation, person, (l) => {
        val future = system.scheduler.scheduleOnce(5 second, self, FiveSeconds)
        person ! new SimpleCommand("г помогу")
        become(waitTime(future))
      })
  }

  private def waitTime(future: Cancellable): Receive = {
    case FiveSeconds =>
      println("### QUEST NOT RESPONDING")
      finishQuest(person)
      become(quest)
    case RawRead(text) if text.matches(QuestTakenPattern) =>
      person ! RoamArea(Set(assists), persister.locationByMob(assists.fullName).toSet)
      future.cancel()
      become(waitRoamFinish)
  }

  private def waitRoamFinish: Receive = {
    case RoamingFinished =>
      person ! KillMobRequest(mainRogue)
      become(waitKill)
  }

  import Mob._
  private def waitKill: Receive = {
    case KillEvent(shortName, _, _, _) if shortName === mainRogue.shortName.get =>
      person ! new SimpleCommand("взять глав")
      goAndDo(questerLocation, person, (l) => {
        person ! new SimpleCommand("дать труп стар")
        finishQuest(person)
      })
  }
}

case object FiveSeconds
