package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper._
import com.tort.mudai.person._
import scalaz._
import Scalaz._
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.command.RequestWalkCommand
import com.tort.mudai.mapper.MoveEvent
import scala.concurrent.duration._
import Mob._

class Prospection(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {
  import Location._
  import context._

  def receive = waitStartCommand

  def waitStartCommand: Receive = {
    case StartQuest =>
      person ! RequestPulses
      visitProspectableArea
  }

  private val bank = persister.locationByTitle("Дом ростовщика").head
  private val zone = persister.zoneByName(Zone.name("Искоростень"))
  private val forgery = persister.locationByTitleAndZone(title("Кузня"), zone).head
  private val from: Location = persister.loadLocation(locationId("f080eeef-84ba-43f8-b440-0ed9610bfab1"))
  private val to: Location = persister.loadLocation(locationId("0a96b963-9f2d-448f-8f71-44ba014c8782"))
  private val path = pathHelper.pathTo(from.some, to)

  private val Stone = "камушек"
  private val Storage = "сундучок"
  private val GlassContainer = "сума.снег"
  private val GemContainer = "призр"
  private val FoundGemsContainer = "перемет"
  
  private val digForYellowGems = true

  private def visitProspectableArea = {
    goAndDo(from, person, (l) => {
      dig(path, 0)
    })
  }

  private def findAnotherDiggable(restOfPath: List[String @@ Direction], times: Int) = {
    restOfPath match {
      case Nil =>
        goAndDo(from, person, (l) => {
          dig(path, times)
        })
      case x :: xs =>
        person ! RequestWalkCommand(x)
        become(waitMove(xs, times))
    }
  }

  private def waitMove(path: List[String @@ Direction], times: Int): Receive = {
    case MoveEvent(_, _, _) =>
      dig(path, times)
  }

  private def dig(path: List[String @@ Direction], times: Int) = {
    if (times > 70) {
      person ! new SimpleCommand("кол !почин! кирка")
      become(waitDig(path, 0))
      system.scheduler.scheduleOnce(3 second, self, Dig)
    } else {
      become(waitDig(path, times + 1))
      system.scheduler.scheduleOnce(3 second, self, Dig)
    }
  }

  private case object Dig

  private def waitDig(path: List[String @@ Direction], times: Int): Receive = {
    case Dig =>
      become(waitPulse(path, times))
    case RawRead(text) if text.matches("(?ms).* камушек не помещается туда..*") =>
      emptyContainer(times)
  }

  private def waitPulse(path: List[String @@ Direction], times: Int): Receive = {
    case Pulse =>
      person ! DigCommand
      become(waitDigResult(path, times))
    case RawRead(text) if text.matches("(?ms).* камушек не помещается туда..*") =>
      emptyContainer(times)
  }

  private def emptyContainer(times: Int) {
    goAndDo(bank, person, (l) => {
      storeYellowGems {
        () => sellTrashGems {
          () => continueDigging(times)
        }
      }
    })
  }

  private def storeYellowGems(onFinish: () => Unit) {
    takeGemsFromBag {
      () => sortGems {
        () => moveYellowGemsToStorage(onFinish)
      }
    }
  }

  private def moveYellowGemsToStorage(onFinish: () => Unit) {
    person ! new SimpleCommand(s"вз все.желт $GemContainer")
    person ! new SimpleCommand(s"полож все.желт $Storage")
    onFinish()
  }

  private def takeGemsFromBag(onFinish: () => Unit) {
    person ! new SimpleCommand(s"взять 10 $Stone $FoundGemsContainer")
    become(waitTakeGems(() => sortGems(() => takeGemsFromBag(onFinish)), onFinish))
  }

  private def sortGems(onFinish: () => Unit) {
    person ! new SimpleCommand(s"осм $Stone")
    become(waitGlanceGem(onFinish))
  }

  def waitGlanceGem(onFinish: () => Unit): Receive = {
    case RawRead(text) if text.matches("(?ms).*ДРАГ.КАМЕНЬ.*") =>
      person ! new SimpleCommand(s"полож $Stone $GemContainer")
      become(waitPutGemToBag(onFinish))
    case RawRead(text) if text.matches("(?ms).*СТЕКЛО.*") =>
      person ! new SimpleCommand(s"полож $Stone $GlassContainer")
      become(waitPutGemToBag(onFinish))
    case RawRead(text) if text.matches("(?ms).*Похоже, этого здесь нет!.*") =>
      onFinish()
  }

  def waitPutGemToBag(onFinish: () => Unit): Receive = {
    case RawRead(text) if text.matches("(?ms).*Вы положили .* камушек в .*") =>
      sortGems(onFinish)
  }

  private def sellTrashGems(onFinish: () => Unit) {
    val nextAction = digForYellowGems match {
      case true => () => sellGems(GemContainer)(onFinish)
      case false => onFinish
    }

    goAndDo(forgery, person, (l) => {
      sellGems(GlassContainer) {
        nextAction
      }
    })
  }

  private def sellGems(container: String)(onFinish: () => Unit) {
    person ! new SimpleCommand(s"вз $Stone $container")
    person ! new SimpleCommand(s"прод $Stone")
    become(waitTakeGems(() => sellGems(container)(onFinish), onFinish))
  }

  private def waitTakeGems(onTake: () => Unit, onFinish: () => Unit): Receive = {
    case RawRead(text) if text.matches(s"(?ms).*Вы взяли .* $Stone из .*") =>
      onTake()
    case RawRead(text) if text.matches(s"(?ms).*Вы не видите '$Stone' в .*") =>
      onFinish()
  }

  private def continueDigging(times: Int) {
    goAndDo(from, person, (l) => {
      dig(path, times)
    })
  }

  private val MobFoundPattern = "(?ms).*Вы выкопали (.*)!.*".r

  private def waitDigResult(path: List[String @@ Direction], times: Int): Receive = {
    case RawRead(text) if text.matches("(?ms).*Вы нашли (?:[^\n]*) камушек!.*") =>
      person ! new SimpleCommand(s"полож кам $FoundGemsContainer")
      dig(path, times)
    case RawRead(text) if text.matches("(?ms).*Вы нашли старую кирку!.*") =>
      person ! new SimpleCommand(s"полож кирк $GemContainer")
      dig(path, times)
    case RawRead(text) if text.matches("(?ms).*Вы нашли могилку кладоискателя!.*") =>
      person ! new SimpleCommand("брос могил")
    case RawRead(text) if text.matches("(?ms).*Вы нашли (?:[^\n]*)!.*") =>
      dig(path, times)
    case RawRead(text) if text.matches("(?ms).*Тут и так все перекопано..*") =>
      findAnotherDiggable(path, times)
    case RawRead(text) if text.matches("(?ms).* камушек не помещается туда..*") =>
      emptyContainer(times)
    case RawRead(text) if text.matches("(?ms).*Вы выкопали (?:.*)!.*") =>
      val MobFoundPattern(mobAccusatif) = text
      person ! new SimpleCommand("смотр")
      persister.mobByAccusative(accusative(mobAccusatif)) match {
        case None =>
          mobAccusatif.split(" ").foreach(x => person ! new SimpleCommand("прик все убить " + x.dropRight(2)))
        case Some(mob) =>
          person ! Attack(mob)
          dig(path, times)
      }
    case RawRead(text) if text.matches("(?ms).*Вы стали усердно ковырять каменистую почву....*") =>
      dig(path, times)
  }
}

case object DigCommand extends RenderableCommand {
  def render = "копать"
}
