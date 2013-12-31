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
      become(dig(path, 0))
    })
  }

  private def findAnotherDiggable(path: List[String @@ Direction], times: Int) = {
    path match {
      case Nil =>
        become(waitStartCommand)
      case x :: xs =>
        person ! RequestWalkCommand(x)
        become(waitMove(xs, times))
    }
  }

  private def waitMove(path: List[String @@ Direction], times: Int): Receive = {
    case MoveEvent(_, _, _) =>
      become(dig(path, times))
  }

  private def dig(path: List[String @@ Direction], times: Int): Receive = {
    case Pulse =>
      system.scheduler.scheduleOnce(2 second, self, Dig)
    case Dig =>
      if (times > 70) {
        person ! new SimpleCommand("кол !почин! кирка")
        person ! DigCommand
        become(waitDigResult(path, 0))
      } else {
        person ! DigCommand
        become(waitDigResult(path, times + 1))
      }
  }

  private case object Dig

  private val MobFoundPattern = "(?ms).*Вы выкопали (.*)!.*".r

  private def waitDigResult(path: List[String @@ Direction], times: Int): Receive = {
    case RawRead(text) if text.matches("(?ms).*Вы только зря расковыряли землю и раскидали камни..*") =>
      become(dig(path, times))
    case RawRead(text) if text.matches("(?ms).*Вы долго копали, но так и не нашли ничего полезного..*") =>
      become(dig(path, times))
    case RawRead(text) if text.matches("(?ms).*Вы нашли (?:[^\n]*) камушек!.*") =>
      person ! new SimpleCommand("полож кам перемет")
      become(dig(path, times))
    case RawRead(text) if text.matches("(?ms).*Вы нашли (?:[^\n]*)!.*") =>
      become(dig(path, times))
    case RawRead(text) if text.matches("(?ms).*Тут и так все перекопано..*") =>
      findAnotherDiggable(path, times)
    case RawRead(text) if text.matches("(?ms).*Вы выкопали (?:.*)!.*") =>
      val MobFoundPattern(mob) = text
      person ! new SimpleCommand("прик все убить " + mob.dropRight(2))
  }
}

case object DigCommand extends RenderableCommand {
  def render = "копать"
}
