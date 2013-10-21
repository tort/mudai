package com.tort.mudai.quest

import akka.actor.{Cancellable, ActorRef}
import com.tort.mudai.mapper.{Mob, PathHelper, LocationPersister}
import com.tort.mudai.person._
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.RoamArea
import com.tort.mudai.event.KillEvent
import com.tort.mudai.command.SimpleCommand
import scalaz._
import Scalaz._
import akka.util.Timeout
import scala.concurrent.duration._

class RogueForestQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  val rogues = Set(
    "Огромных размеров человек, в грязной одежде и с огромной, увесистой дубиной в руках.",
    "Грязный человек, явно разбойничьей наружности пристально смотрит на вас.",
    "Здоровенный детина, пристально смотрит на вас.",
    "Пьяный разбойник неспешно бредет куда-то."
  ).map(persister.mobByFullName(_).get)

  val mainRogue = persister.mobByFullName("Здоровенный детина внимательно рассматривает вас.").get

  val roguesHabitation = Set(
    "Неприметная тропа",
    "В землянке",
    "В лагере разбойников",
    "К шалашу",
    "В шалаше",
    "У лаза",
    "Подземный лаз"
  ).flatMap(fullName => persister.locationByTitle(fullName)) -- persister.locationByMob(mainRogue.fullName)

  val quester = "Крепкого вида дедок, внимательно смотрит на вас."
  val questerLocation = persister.locationByMob(quester).head

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      person ! RequestPulses
      goAndDo(questerLocation, person, (l) => {
        val future = system.scheduler.scheduleOnce(5 second, self, FiveSeconds)
        person ! new SimpleCommand("г помогу")
        become(waitQuestPrompt(future))
      })
  }

  def waitQuestPrompt(future: Cancellable): Receive = {
    case FiveSeconds =>
      println("### QUEST NOT RESPONDING")
      finishQuest(person)
      become(quest)
    case RawRead(text) if text.matches("(?ms).*Глава поселения сказал : 'А инструменты ежели обнаружите - уж мне принесите, я кузнецу передам...'.*") =>
      future.cancel()
      person ! RoamArea(rogues, roguesHabitation)
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
      goAndDo(questerLocation, person, (l) => {
        person ! new SimpleCommand("дать инструм глав")
        finishQuest(person)
      })
  }
}
