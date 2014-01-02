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

  private def visitProspectableArea = {
    val from: Location = persister.loadLocation(locationId("f080eeef-84ba-43f8-b440-0ed9610bfab1"))
    val to: Location = persister.loadLocation(locationId("0a96b963-9f2d-448f-8f71-44ba014c8782"))
    val path = pathHelper.pathTo(from.some, to)

    goAndDo(from, person, (l) => {
      dig(path, 0)
    })
  }

  private def findAnotherDiggable(path: List[String @@ Direction], times: Int) = {
    path match {
      case Nil =>
        become(waitStartCommand)
        finishQuest(person)
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
        system.scheduler.scheduleOnce(2500 millisecond, self, Dig)
      } else {
        become(waitDig(path, times + 1))
        system.scheduler.scheduleOnce(2500 millisecond, self, Dig)
      }
  }

  private case object Dig

  private def waitDig(path: List[String @@ Direction], times: Int): Receive = {
    case Dig =>
      become(waitPulse(path, times))
  }

  private def waitPulse(path: List[String @@ Direction], times: Int): Receive = {
    case Pulse =>
      person ! DigCommand
      become(waitDigResult(path, times))
  }

  private val MobFoundPattern = "(?ms).*Вы выкопали (.*)!.*".r

  private def waitDigResult(path: List[String @@ Direction], times: Int): Receive = {
    case RawRead(text) if text.matches("(?ms).*Вы только зря расковыряли землю и раскидали камни..*") =>
      dig(path, times)
    case RawRead(text) if text.matches("(?ms).*Вы долго копали, но так и не нашли ничего полезного..*") =>
      dig(path, times)
    case RawRead(text) if text.matches("(?ms).*Вы нашли (?:[^\n]*) камушек!.*") =>
      person ! new SimpleCommand("полож кам перемет")
      dig(path, times)
    case RawRead(text) if text.matches("(?ms).*Вы нашли (?:[^\n]*)!.*") =>
      dig(path, times)
    case RawRead(text) if text.matches("(?ms).*Тут и так все перекопано..*") =>
      findAnotherDiggable(path, times)
    case RawRead(text) if text.matches("(?ms).*Вы выкопали (?:.*)!.*") =>
      val MobFoundPattern(mobGenitif) = text
      person ! new SimpleCommand("смотр")
      persister.mobByGenitive(genitive(mobGenitif)) match {
        case None =>
          mobGenitif.split(" ").foreach(x => person ! new SimpleCommand("прик все убить " + x.dropRight(2)) )
        case Some(mob) =>
          person ! KillMobRequest(mob)
      }
  }
}

case object DigCommand extends RenderableCommand {
  def render = "копать"
}
