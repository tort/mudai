package com.tort.mudai.quest

import com.tort.mudai.person._
import akka.actor.{Cancellable, ActorRef}
import com.tort.mudai.mapper.{Mob, Location, LocationPersister, PathHelper}
import com.tort.mudai.command.SimpleCommand
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.event.KillEvent
import scalaz._
import Scalaz._
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.KillMobRequest
import com.tort.mudai.event.KillEvent

class OldHunterQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  def receive = quest

  val quester: Location = persister.locationByMob("Старый охотник подслеповато щурясь, смотрит на Вас.").head

  val bear = persister.mobByFullName("Огромный медведь развалился здесь в своей берлоге.").head

  val QuestPrompt = """(?ms).*Старый охотник сказал : 'Я в долгу не останусь!'.*"""

  def quest: Receive = {
    case StartQuest =>
      println("QUEST STARTED")
      person ! RequestPulses

      goAndDo(quester, person, (l) => {
        val future = system.scheduler.scheduleOnce(5 second, self, FiveSeconds)
        become(waitQuestPrompt(future))
        person ! new SimpleCommand("г помогу")
      })
  }

  private def waitQuestPrompt(future: Cancellable): Receive = {
    case RawRead(text) if text.matches(QuestPrompt) =>
      future.cancel()
      person ! KillMobRequest(bear)
      become(waitKill)
    case FiveSeconds =>
      println("### QUEST NOT RESPONDING")
      finishQuest(person)
      become(quest)
  }

  import Mob._
  private def waitKill: Receive = {
    case KillEvent(shortName, _, _, _) if shortName === bear.shortName.get =>
      goAndDo(quester, person, (l) => {
        person ! new SimpleCommand("дать шкур охот")
        finishQuest(person)
      })
  }
}
