package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper._
import com.tort.mudai.person._
import akka.util.Timeout
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.RoamArea

class WoodpeckersQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper with ReachabilityHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  val zone = persister.zoneByName(Zone.name("Дятлы"))
  val bearLocation: Location = persister.locationByTitleAndZone("Лесная опушка", zone).head
  val bearLair = persister.locationByTitleAndZone("Медвежья берлога", zone).head
  val logjam = persister.loadLocation("9fe7abd5-1ca3-4933-82af-0447a41696ed")
  val littleAnt = persister.locationByTitleAndZone("У огромного камня", zone).head
  val thronePlace = persister.locationByTitleAndZone("У трона королевы", zone).head
  val meadowBeforeAnthill = persister.locationByTitleAndZone("Поляна перед муравейником", zone).head
  val pillagedAnthill = reachableFrom(thronePlace, persister.nonBorderNonLockableNeighbors, Set(meadowBeforeAnthill)).map(x => persister.loadLocation(x))
  val blackAnts = Set(
    "Черный муравей скачет по тропе на огромной боевой тле.",
    "Черный муравей прохаживается по муравейнику.",
    "Гигантский черный муравей неспеша пожирает труп.",
    "Черный муравей торопливо разбивает уцелевшие яйца."
  ).map(persister.mobByFullName(_).get)
  val blackAnthillGates = persister.locationByTitleAndZone("Поваленный ствол сосны", zone).head

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      println("QUEST STARTED")
      person ! RequestPulses

      goAndDo(bearLocation, person, (l) => {
        become(waitBear)
      })
  }

  private def waitBear: Receive = {
    case RawRead(text) if text.matches("(?ms).*Медведь мечтательно облизнулся и замер в ожидании спасительной выпивки..*") =>
      person ! new SimpleCommand("дать четверть медведь")
      become(waitEntranceOpened)
  }

  private def waitEntranceOpened: Receive = {
    case RawRead(text) if text.matches("(?ms).*Медведь побрел обратно в лес, оставляя за собой поваленные деревья..*") =>
      goAndDo(bearLair, person, (l) => {
        become(waitPrompt)
      })
  }

  private def waitPrompt: Receive = {
    case RawRead(text) if text.matches("(?ms).*Леший сказал : 'Не мог бы ты помочь вернуть их?'.*") =>
      person ! new SimpleCommand("г помогу")
      become(waitForGrass)
  }

  private def waitForGrass: Receive = {
    case RawRead(text) if text.matches("(?ms).*Леший сказал : 'Когда спасешь лес, приходи ко мне и потолкуем о спасении белок!'.*") =>
      goAndDo(logjam, person, (l) => {
        person ! new SimpleCommand("брос трава")
        goAndDo(bearLair, person, (l) => {
          system.scheduler.scheduleOnce(3 second, self, FiveSeconds)
          become(waitExplosion)
        })
      })
  }

  private def waitExplosion: Receive = {
    case FiveSeconds =>
      goAndDo(littleAnt, person, (l) => {
        become(waitAntPrompt)
      })
  }

  private def waitAntPrompt: Receive = {
    case RawRead(text) if text.matches("(?ms).*Глаза муравьишки загорелись надеждой..*") =>
      person ! new SimpleCommand("г помогу")
      become(waitAntExplanation)
  }

  private def waitAntExplanation: Receive = {
    case RawRead(text) if text.matches("(?ms).*Рыжий муравьишка сказал : 'Возможно, эти тайные слова помогут тебе спасти мою сестренку.'.*") =>
      person ! RoamArea(blackAnts, pillagedAnthill)
      become(waitRoamFinish)
  }

  private def waitRoamFinish: Receive = {
    case RoamingFinished =>
      goAndDo(blackAnthillGates, person, (l) => {
        person ! new SimpleCommand("стучать ворота")
        become(waitPassRequest)
      })
  }

  private def waitPassRequest: Receive = {
    case RawRead(text) if text.matches("(?ms).*Услышав удары, хриплый голос за воротами произнес: \"Кто там?\".*") =>
      person ! new SimpleCommand("г свои")
      become(waitGatesOpened)
  }

  private def waitGatesOpened: Receive = {
    case RawRead(text) if text.matches("(?ms).*Створки ворот со скрипом разошлись в стороны..*") =>
//      person ! RoamArea(blackAnts, blackAnthill)
  }
}
