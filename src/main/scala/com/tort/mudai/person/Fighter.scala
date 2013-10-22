package com.tort.mudai.person

import akka.actor.{ActorRef, Props, Actor}
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.mapper._
import scalaz._
import Scalaz._
import com.tort.mudai.mapper.Mob.ShortName
import com.tort.mudai.command.RequestWalkCommand
import com.tort.mudai.event.TargetFleeEvent
import com.tort.mudai.event.KillEvent
import scala.Some
import com.tort.mudai.event.MemFinishedEvent
import com.tort.mudai.event.GroupStatusEvent
import com.tort.mudai.event.CurseSucceededEvent
import com.tort.mudai.event.TargetAssistedEvent
import com.tort.mudai.mapper.MoveEvent
import com.tort.mudai.event.CurseFailedEvent
import com.tort.mudai.event.DisarmAssistantEvent

class Fighter(person: ActorRef, persister: LocationPersister) extends Actor {

  import context._

  val antiBasher = actorOf(Props(classOf[AntiBasher]))
  val curser = actorOf(Props(classOf[Curser]))
  val fleeker = actorOf(Props(classOf[Fleeker]))

  def receive = rec

  def rec: Receive = {
    case MemFinishedEvent() =>
      person ! new SimpleCommand("вст")
      person ! ReadyForFight
    case e@Attack(target) =>
      person ! RequestPulses
      person ! new SimpleCommand(s"прик все убить $target")

      antiBasher ! e
      curser ! e
      fleeker ! e
    case Assist =>
      person ! RequestPulses
      person ! new SimpleCommand("прик все пом")
    case KillEvent(target, exp, _, _) =>
      person ! new SimpleCommand("группа")
      become(waitGroupStatus)
    case GroupStatusEvent(name, health, _, status) if status === "Стоит" || status === "Сидит" =>
      healOnStatus(name, health)
    case TargetFleeEvent(target, direction) =>
      become(waitPulse(target, direction))
    case DisarmAssistantEvent(_, _, _) =>
      person ! new SimpleCommand("взять чудск")
      person ! new SimpleCommand("дать чудск дружинник")
      person ! new SimpleCommand("прик все воор чудск")
    case TargetAssistedEvent(assister, targetGenitive) =>
      for {
        mob <- persister.mobByShortName(assister)
        alias <- mob.alias
      } yield person ! CurseCommand(alias)
    case RequestPulses => person ! RequestPulses
    case YieldPulses => person ! YieldPulses
    case c: RenderableCommand if sender == antiBasher => person ! c
    case c: RenderableCommand if sender == curser => person ! c
    case c: RenderableCommand if sender == fleeker => person ! c
    case c: FleeMove if sender == fleeker => person ! c
    case e =>
      antiBasher ! e
      curser ! e
      fleeker ! e
  }

  def waitGroupStatus: Receive = {
    case GroupStatusEvent(name, health, _, status) if status === "Стоит" || status === "Сидит" =>
      if (status === "Сидит") {
        person ! new SimpleCommand("прик все встать")
      }

      healOnStatus(name, health)

      person ! YieldPulses
      become(rec)
    case GroupStatusEvent(_, _, _, status) =>
      println(status)
      become(rec)
  }


  private def healOnStatus(name: String @@ ShortName, health: String) {
    if ((health.trim === "Ранен") || (health.trim === "Лег.ранен") || (health.trim === "Тяж.ранен") || (health.trim === "Оч.тяж.ранен") || (health.trim === "При смерти")) {
      val mob: Option[Mob] = persister.mobByShortName(name)
      mob.flatMap(_.alias) foreach {
        case alias => person ! new SimpleCommand(s"кол !к и! $alias")
      }
    }
  }

  def waitPulse(target: String @@ ShortName, direction: String @@ Direction): Receive = {
    case Pulse =>
      person ! RequestWalkCommand(direction)
      become(waitMove(target))
    case KillEvent(_, exp, _, _) =>
      person ! new SimpleCommand("группа")
      become(waitGroupStatus)
  }

  def waitMove(target: String @@ ShortName): Receive = {
    case MoveEvent(from, Some(direction), to) =>
      persister.mobByShortName(target).flatMap(_.alias) match {
        case None => println(s"### UNKNOWN ALIAS FOR $target")
        case Some(alias) =>
          person ! new SimpleCommand(s"прик все убить $alias")
      }
      become(rec)
  }
}

class Curser extends Actor {

  import context._

  def receive = {
    case Attack(target) =>
      sender ! CurseCommand(target)
      become {
        case CurseFailedEvent() =>
          sender ! CurseCommand(target)
        case CurseSucceededEvent(_) =>
          unbecome()
      }
  }
}

case class FleeMove(from: Location, direction: String @@ Direction, to: Location)

case class CurseCommand(target: String) extends RenderableCommand {
  def render = s"кол !прок! $target"
}
