package com.tort.mudai.quest

import akka.actor.{Cancellable, ActorRef}
import com.tort.mudai.mapper._
import com.tort.mudai.person._
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
import Mob._
import Location._

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

  val mainRogue = persister.mobByFullName("Здоровенный детина внимательно рассматривает вас.").head

  val roguesHabitation = reachableFrom(
    persister.locationByTitleAndZone(title("В лагере разбойников"), zone).head,
    persister.nonBorderNonLockableNeighbors,
    Set(persister.locationByMob(mainRogue.fullName).head, persister.locationByTitleAndZone(title("У дубовых ворот"), zone).head)
  ).map(id => persister.loadLocation(id))

  val quester = "Крепкого вида дедок, внимательно смотрит на вас."
  val questerLocation = persister.locationByMob(quester).head

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      person ! RequestPulses
      goAndDo(questerLocation, person, (l) => {
        val future = system.scheduler.scheduleOnce(10 second, self, FiveSeconds)
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
      println(s"START ROAM ROGUES: ${roguesHabitation.size} rooms")
      person ! RoamArea(rogues, roguesHabitation)
      become(waitRoamFinish())
  }

  def waitRoamFinish(): Receive = {
    case RoamingFinished =>
      person ! KillMobRequest(mainRogue)
      become(waitKill)
  }


  private def waitKill: Receive = {
    case KillEvent(shortName, _, _, _) if shortName === mainRogue.shortName.get =>
      goAndDo(questerLocation, person, (l) => {
        person ! new SimpleCommand("дать инструм глав")
        become(waitReward)
      })
  }

  private def waitReward: Receive = {
    case RawRead(text) if text.matches("(?ms).*Глава поселения дал вам мешок денег..*") =>
      person ! new SimpleCommand("брос меш")
      person ! new SimpleCommand("взять меш")
      finishQuest(person)
      become(quest)
  }
}
