package com.tort.mudai.quest

import akka.actor.{Cancellable, ActorRef}
import com.tort.mudai.mapper._
import com.tort.mudai.person._
import scala.concurrent.duration._
import com.tort.mudai.command.SimpleCommand
import scalaz._
import Scalaz._
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.KillMobRequest
import com.tort.mudai.event.KillEvent
import Location._
import Mob._

class AwfulTaleQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

  val zone = persister.zoneByName(Zone.name("Страшная сказка"))
  val questerLocation = persister.locationByTitleAndZone(title("На старом клене"), zone).head
  val hedgehog = persister.mobByFullName("Еж величиной с хорошую избу сейчас растерзает вас в клочья.").head
  val evilSorcerer = persister.mobByFullName("Сгорбленный старичок стоит в углу.").head

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      println("QUEST STARTED")
      person ! RequestPulses

      goAndDo(questerLocation, person, (l) => {
        val future = system.scheduler.scheduleOnce(5 seconds, self, TimeOut)
        become(waitPrompt(future))
      })
  }

  private def waitPrompt(future: Cancellable): Receive = {
    case RawRead(text) if text.matches("(?ms).*Но если храбр ты и силен, можешь попробовать помочь..*") =>
      person ! new SimpleCommand("г помогу")
      become(waitResponse)
      future.cancel()
    case TimeOut =>
      println("### QUEST DO NOT RESPOND")
      finishQuest(person)
      become(quest)
  }

  private def waitResponse: Receive = {
    case RawRead(text) if text.matches("(?ms).*Мы хоть и всю жизнь в лесу живем, а одарить тебя чем найдется..*") =>
      person ! KillMobRequest(hedgehog)
      become(waitKillHedgehog)
  }

  private def waitKillHedgehog: Receive = {
    case KillEvent(shortName, _, _, _) if shortName === hedgehog.shortName.get =>
      person ! new SimpleCommand("встать")
    case RoamingFinished =>
      person ! KillMobRequest(evilSorcerer)
      become(waitKillSorcerer)
  }

  private def waitKillSorcerer: Receive = {
    case KillEvent(shortName, _, _, _) if shortName === evilSorcerer.shortName.get =>
      goAndDo(questerLocation, person, (l) => {
        println("### QUEST FINISHED")
        finishQuest(person)
        become(quest)
      })
  }
}
