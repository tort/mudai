package com.tort.mudai.quest

import akka.actor.{Cancellable, ActorRef}
import com.tort.mudai.mapper._
import com.tort.mudai.person._
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.RoamArea
import com.tort.mudai.event.KillEvent
import scalaz._
import Scalaz._
import Mob._
import Location._

class WoodpeckersQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper with ReachabilityHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  val zone = persister.zoneByName(Zone.name("Дятлы"))
  val zoneEntrance: Location = persister.locationByTitleAndZone(title("Лесная опушка"), zone).head
  val bearLair = persister.locationByTitleAndZone(title("Медвежья берлога"), zone).head
  val logjam = persister.loadLocation(locationId("9fe7abd5-1ca3-4933-82af-0447a41696ed"))
  val littleAnt = persister.locationByTitleAndZone(title("У огромного камня"), zone).head
  val thronePlace = persister.locationByTitleAndZone(title("У трона королевы"), zone).head
  val meadowBeforeAnthill = persister.locationByTitleAndZone(title("Поляна перед муравейником"), zone).head
  val pillagedAnthill = reachableFrom(thronePlace, persister.nonBorderNonLockableNeighbors, Set(meadowBeforeAnthill)).map(x => persister.loadLocation(x))
  val blackAnts = Set(
    "Черный муравей скачет по тропе на огромной боевой тле.",
    "Черный муравей прохаживается по муравейнику.",
    "Гигантский черный муравей неспеша пожирает труп.",
    "Черный муравей торопливо разбивает уцелевшие яйца.",
    "Черный муравей тщательно охраняет ворота.",
    "Черный муравей бредет по своим делам.",
    "Черный муравей навострил усики, почуяв опасность.",
    "Груженый желудями кузнечик ползет в кладовую.",
    "Черная муравьиха пристально следит за ячейками.",
    "Мохналапый муравей готовится съесть рыжее яйцо."
  ).map(fullName).map(persister.mobByFullName(_).get)
  val blackAnthillGates = persister.locationByTitleAndZone(title("Поваленный ствол сосны"), zone).head
  val blackAntQueenRoom = persister.locationByMob("Черная королева-матка восседает на троне.").head
  val blackAntQueen = persister.mobByFullName(fullName("Черная королева-матка восседает на троне.")).head
  val oldHollow = persister.locationByTitleAndZone(title("В старом дупле"), zone).head
  val blackAnthillArea = reachableFrom(
    oldHollow,
    persister.nonBorderNonLockableNeighbors,
    Set(blackAnthillGates, blackAntQueenRoom)).map(x => persister.loadLocation(x))
  val foresterLair = persister.locationByMob("Пучеглазый леший мутным взглядом обводит берлогу.").head
  val onGreatOak = persister.locationByTitleAndZone(title("На Царь-Дубе"), zone).head
  val underGreatOak = persister.locationByTitleAndZone(title("У подножия Царь-Дуба"), zone).head
  val greatOak = reachableFrom(
    onGreatOak,
    persister.nonBorderNonLockableNeighbors,
    Set(underGreatOak)).map(x => persister.loadLocation(x))
  val onGreatBirch = persister.locationByTitleAndZone(title("На Царь-Березе"), zone).head
  val underGreatBirch = persister.locationByTitleAndZone(title("У подножия Царь-Березы"), zone).head
  val woodpeckersKingFullName: String @@ FullName = fullName("Царь Дятлов негодующе машет крыльями.")
  val woodpeckersKingLocation = persister.locationByMob(woodpeckersKingFullName).head
  val woodpeckersKing = persister.mobByFullName(woodpeckersKingFullName).head
  val greatBirch = reachableFrom(
    onGreatBirch,
    persister.nonBorderNonLockableNeighbors,
    Set(underGreatBirch, woodpeckersKingLocation)
  ).map(x => persister.loadLocation(x))
  val woodpeckers = Set(
    "(летит) Белокрылый дятел прячется в листве дерева.",
    "Старый дятел бредет по ветке, часто спотыкаясь.",
    "(летит) Плотоядный дятел грызет чью-то косточку.",
    "Златоклювый дятел беспечно прыгает по ветке.",
    "Пятнистая кукушка о чем-то печально кукует.",
    "Молодой дятел разминает крылья перед полетом.",
    "Бескрылый дятел бежит по ветке в поисках пищи.",
    "(летит) Плотоядный дятел тщательно чистит о листву клюв.",
    "(летит) Пестрый дятел долбит клювом дерево."
  ).map(fullName(_)).map(persister.mobByFullName(_).get)

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      println("QUEST STARTED")
      person ! RequestPulses

      goAndDo(zoneEntrance, person, (l) => {
        val future = system.scheduler.scheduleOnce(15 seconds, self, TimeOut)
        become(waitBear(future))
      })
  }

  private def waitBear(future: Cancellable): Receive = {
    case RawRead(text) if text.matches("(?ms).*Медведь мечтательно облизнулся и замер в ожидании спасительной выпивки..*") =>
      person ! new SimpleCommand("дать четверть медведь")
      become(waitEntranceOpened)
      future.cancel()
    case TimeOut =>
      println("### QUEST NOT RESPONDING")
      finishQuest(person)
      become(quest)
  }

  private def waitEntranceOpened: Receive = {
    case RawRead(text) if text.matches("(?ms).*Медведь побрел обратно в лес, оставляя за собой поваленные деревья..*") =>
      goAndDo(bearLair, person, (l) => {
        become(waitPrompt)
      })
  }

  private def waitPrompt: Receive = {
    case RawRead(text) if text.matches("(?ms).*Леший сказал : 'Не мог бы ты помочь вернуть их\\?'.*") =>
      person ! new SimpleCommand("г помогу")
      become(waitForGrass)
  }

  private def waitForGrass: Receive = {
    case RawRead(text) if text.matches("(?ms).*Леший сказал : 'Когда спасешь лес, приходи ко мне и потолкуем о спасении белок\\!'.*") =>
      goAndDo(logjam, person, (l) => {
        person ! new SimpleCommand("брос трава")
        goAndDo(bearLair, person, (l) => {
          system.scheduler.scheduleOnce(5 second, self, FiveSeconds)
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
    case RawRead(text) if text.matches("(?ms).*Услышав удары, хриплый голос за воротами произнес: \"Кто там\\?\".*") =>
      person ! new SimpleCommand("г свои")
      become(waitGatesOpened)
  }

  private def waitGatesOpened: Receive = {
    case RawRead(text) if text.matches("(?ms).*Створки ворот со скрипом разошлись в стороны..*") =>
      person ! RoamArea(blackAnts, blackAnthillArea)
      become(waitFinishRoamBlackAnthill)
  }

  private def waitFinishRoamBlackAnthill: Receive = {
    case RoamingFinished =>
      person ! KillMobRequest(blackAntQueen)
      become(waitKillBlackAntQueen)
  }

  private def waitKillBlackAntQueen: Receive = {
    case KillEvent(shortName, _, _, _) if shortName === blackAntQueen.shortName.get =>
      goAndDo(littleAnt, person, (l) => {
        person ! new SimpleCommand("дать яйцо мурав")
        system.scheduler.scheduleOnce(10 seconds, self, FiveSeconds)
        become(waitRewardFromLittleAnt)
      })
  }

  private def waitRewardFromLittleAnt: Receive = {
    case FiveSeconds =>
      person ! new SimpleCommand("брос мешоч")
      person ! new SimpleCommand("вз мешоч")
      goAndDo(bearLair, person, (l) => {
        become(waitPentagram)
      })
  }

  private def waitPentagram: Receive = {
    case RawRead(text) if text.matches("(?ms).*Леший сказал : 'ступай\\!'.*") =>
      person ! new SimpleCommand("прик все войти пент")
      goAndDo(foresterLair, person, (l) => {
        become(waitForesterPrompt)
      })
  }

  private def waitForesterPrompt: Receive = {
    case RawRead(text) if text.matches("(?ms).*Пучеглазый Леший сказал : 'И я так и быть отпущу с тобой белок. А теперь ступай...'.*") =>
      person ! RoamArea(woodpeckers, greatOak)
      become(waitFinishRoamOak)
  }

  private def waitFinishRoamOak: Receive = {
    case RoamingFinished =>
      person ! RoamArea(woodpeckers, greatBirch)
      become(waitFinishRoamBirch)
  }

  private def waitFinishRoamBirch: Receive = {
    case RoamingFinished =>
      person ! KillMobRequest(woodpeckersKing)
      become(waitKillWoodpeckersKing)
  }

  private def waitKillWoodpeckersKing: Receive = {
    case RoamingFinished =>
      goAndDo(foresterLair, person, (l) => {
        person ! new SimpleCommand("дать череп леший")
        become(waitForesterPentagram)
      })
  }

  private def waitForesterPentagram: Receive = {
    case RawRead(text) if text.matches("(?ms).*Лазурная пентаграмма возникла в воздухе..*") =>
      person ! new SimpleCommand("прик все войти пент")
      goAndDo(bearLair, person, (l) => {
        person ! new SimpleCommand("дать связ леший")
        become(waitReward)
      })
  }

  private def waitReward: Receive = {
    case RawRead(text) if text.matches("(?ms).*Леший сказал : 'Спасибо. Выручил ты меня!'.*") =>
      finishQuest(person)
  }
}

case object TimeOut
