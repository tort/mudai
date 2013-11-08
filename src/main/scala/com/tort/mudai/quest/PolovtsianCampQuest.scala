package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper._
import com.tort.mudai.person._
import akka.util.Timeout
import scala.concurrent.duration._
import scalaz._
import Scalaz._
import Mob._
import Location._
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.RoamMobsInArea
import com.tort.mudai.command.SimpleCommand

class PolovtsianCampQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper with ReachabilityHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  val zone = persister.zoneByName(Zone.name("Половцы"))
  val campEntrance = persister.locationByTitleAndZone(title("Вход в лагерь"), zone).head
  val nuker = persister.mobByFullName(fullName("Нукер батыра сидит тут, оберегая покой господина.")).head
  val nukerLocation = persister.locationByMob(nuker.fullName).head
  val shamanLocation = persister.locationByTitleAndZone(title("Шатер шамана"), zone).head
  val area = reachableFrom(campEntrance, persister.nonBorderNonLockableNeighbors, Set(nukerLocation, shamanLocation)).map(x => persister.loadLocation(x))
  val mobs = persister.killableMobsBy(zone)
  val batirLocation = persister.locationByMob("Батыр стоит тут, обдумывая дальнейший путь отряда.").head
  val mainTent = reachableFrom(batirLocation, persister.nonBorderNonLockableNeighbors, Set(nukerLocation)).map(x => persister.loadLocation(x))
  val oldManLocation = persister.locationByMob("Седой старик сидит здесь, отрешенно уставившись на свои руки.").head
  val pit = persister.locationByTitleAndZone(title("Яма"), zone).head
  val shaman = persister.mobByFullName(fullName("Старый половецкий шаман стоит тут, колдуя над костром.")).head

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      println("QUEST STARTED")
      person ! RequestPulses

      person ! RoamMobsInArea(mobs, area)
      become(waitRoamFinish)
  }

  def waitRoamFinish: Receive = {
    case RoamingFinished =>
      person ! KillMobRequest(nuker)
      become(waitKillNuker)
  }

  def waitKillNuker: Receive = {
    case RoamingFinished =>
      person ! new SimpleCommand("вз все все.труп")
      person ! RoamMobsInArea(mobs, mainTent)
      become(waitKillBatir)
  }

  def waitKillBatir: Receive = {
    case RoamingFinished =>
      goAndDo(pit, person, (l) => {
        person ! new SimpleCommand("помочь старик")
        become(waitKey)
      })
  }

  def waitKey: Receive = {
    case RawRead(text) if text.matches("(?ms).*Старик дал вам медный ключ..*") =>
      person ! KillMobRequest(shaman)
      become(waitKillShaman)
  }

  def waitKillShaman: Receive = {
    case RoamingFinished =>
      person ! new SimpleCommand("вз все все.труп")
      person ! new SimpleCommand("отпер сунд")
      person ! new SimpleCommand("откр сунд")
      person ! new SimpleCommand("вз все сунд")
      finishQuest(person)
      become(quest)
  }
}
