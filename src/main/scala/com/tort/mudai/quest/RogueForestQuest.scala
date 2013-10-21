package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper.{Mob, PathHelper, LocationPersister}
import com.tort.mudai.person._
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.RoamArea
import com.tort.mudai.event.KillEvent
import com.tort.mudai.command.SimpleCommand
import scalaz._
import Scalaz._

class RogueForestQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

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
  ).flatMap(fullName => persister.locationByTitle(fullName))

  val quester = "Крепкого вида дедок, внимательно смотрит на вас."
  val questerLocation = persister.locationByMob(quester).head

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      person ! RequestPulses
      goAndDo(questerLocation, person, (l) => {
        person ! new SimpleCommand("г помогу")
        become(waitQuestPrompt)
      })
  }

  def waitQuestPrompt: Receive = {
    case RawRead(text) if text.matches("(?ms).*Глава поселения сказал : 'А инструменты ежели обнаружите - уж мне принесите, я кузнецу передам...'.*") =>
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
