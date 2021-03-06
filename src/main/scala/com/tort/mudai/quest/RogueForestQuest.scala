package com.tort.mudai.quest

import akka.actor.{Cancellable, ActorRef}
import com.tort.mudai.mapper._
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
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.KillMobRequest
import com.tort.mudai.event.KillEvent
import com.tort.mudai.person.RoamArea

class RogueForestQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper with ReachabilityHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  val zone = persister.zoneByName(Zone.name("Разбойничий лес"))

  val rogues = Set(
    "Огромных размеров человек, в грязной одежде и с огромной, увесистой дубиной в руках.",
    "Грязный человек, явно разбойничьей наружности пристально смотрит на вас.",
    "Здоровенный детина, пристально смотрит на вас.",
    "Пьяный разбойник неспешно бредет куда-то."
  ).map(persister.mobByFullName(_).get)

  val mainRogue = persister.mobByFullName("Здоровенный детина внимательно рассматривает вас.").get

  val roguesHabitation = reachableFrom(
    persister.locationByTitleAndZone("В лагере разбойников", zone).head,
    persister.nonBorderNonLockableNeighbors,
    Set(persister.locationByMob(mainRogue.fullName).head, persister.locationByTitleAndZone("У дубовых ворот", zone).head)
  ).flatMap(fullName => persister.locationByTitleAndZone(fullName, zone))

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
        become(quest)
      })
  }
}
