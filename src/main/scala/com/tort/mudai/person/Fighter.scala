package com.tort.mudai.person

import akka.actor.{Cancellable, ActorRef, Props, Actor}
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.mapper._
import scalaz._
import Scalaz._
import com.tort.mudai.mapper.Mob.{Alias, ShortName}
import com.tort.mudai.event._
import scala.concurrent.duration._
import com.tort.mudai.quest.TimeOut
import com.tort.mudai.event.KillEvent
import scala.Some
import com.tort.mudai.event.GroupStatusEvent
import com.tort.mudai.event.CurseSucceededEvent
import com.tort.mudai.event.TargetAssistedEvent
import com.tort.mudai.event.CurseFailedEvent
import com.tort.mudai.event.DisarmAssistantEvent

class Fighter(person: ActorRef, persister: LocationPersister, mapper: ActorRef) extends Actor {

  import context._

  val antiBasher = actorOf(Props(classOf[AntiBasher]))
  val fleeker = actorOf(Props(classOf[Fleeker], mapper))
  val attacker = actorOf(Props(classOf[Attacker]))

  def receive = rec(false)

  /*
   * Define universal criteria for battle start and finish
   */
  def rec(peaceStatus: Boolean): Receive = {
    case e@Attack(target, number) =>
      person ! RequestPulses
      antiBasher ! e
      fleeker ! e
      attacker ! e
    case KillEvent(target, exp, _, _) =>
      person ! new GroupStatusCommand
      become(recoverAfterFight(peaceStatus))
    case GroupStatusEvent(name, health, _, "Стоит") =>
      if (peaceStatus)
        person ! YieldPulses
    case e@FightRoundEvent(_, _, _) =>
      sender ! RequestPulses
      fleeker ! e
      become(rec(peaceStatus = false))
    case e@PeaceStatusEvent() =>
      become(rec(peaceStatus = true))
      fleeker ! e
    case Assist =>
      person ! new SimpleCommand("прик все пом")
    case e@GroupStatusEvent(name, health, _, status) =>
      healOnStatus(name, health)
      attacker ! e
    case DisarmAssistantEvent(_, _, _) =>
      person ! new SimpleCommand("взять лук.хран")
      person ! new SimpleCommand("дать лук.хран гри.рост")
      person ! new SimpleCommand("прик все воор лук")
    case RequestPulses => person ! RequestPulses
    case YieldPulses => person ! YieldPulses
    case c: RenderableCommand if sender == antiBasher => person ! c
    case c: RenderableCommand if sender == fleeker => person ! c
    case c: RenderableCommand if sender == attacker => person ! c
    case e =>
      antiBasher ! e
      fleeker ! e
      attacker ! e
  }

  private def recoverAfterFight(peaceStatus: Boolean): Receive = {
    case GroupStatusEvent(name, health, _, status) if status === "Стоит" || status === "Сидит" || status === "Сражается" =>
      healOnStatus(name, health)

      status match {
        case "Сидит" =>
          person ! new SimpleCommand("прик все встать")
          system.scheduler.scheduleOnce(1 second, person, new GroupStatusCommand)
        case "Стоит" =>
          person ! YieldPulses
          become(rec(peaceStatus))
        case _ =>
      }
    case KillEvent(target, exp, _, _) =>
      person ! new GroupStatusCommand
  }


  private def healOnStatus(name: String @@ ShortName, health: String) {
    if ((health.trim === "Ранен") || (health.trim === "Лег.ранен") || (health.trim === "Тяж.ранен") || (health.trim === "Оч.тяж.ран") || (health.trim === "При смерти")) {
      val mob: Option[Mob] = persister.mobByShortName(name)
      mob.flatMap(_.alias) foreach {
        case alias => person ! new SimpleCommand(s"кол !к и! $alias")
      }
    }
  }
}

class Curser extends Actor {

  import context._

  def receive = {
    case Attack(target, number) =>
      sender ! CurseCommand(target.alias.get)
      become {
        case CurseFailedEvent() =>
          sender ! CurseCommand(target.alias.get)
        case CurseSucceededEvent(_) =>
          unbecome()
      }
  }
}

class Attacker extends Actor {

  import context._

  def receive = rec

  def rec: Receive = {
    case AttackByAlias(target) =>
      sender ! new SimpleCommand(s"прик все убить $target")
    case Attack(target, number) =>
      val alias = number match {
        case None => s"${target.alias.get}"
        case Some(x) => s"${x}.${target.alias.get}"
      }
      sender ! new SimpleCommand(s"прик все убить $alias")
      if (target.canFlee) {
        sender ! new SimpleCommand(s"кол !сеть! $alias")
      }
  }

  def waitGroupEvent(target: String @@ Alias, future: Cancellable): Receive = {
    case OrderFailedEvent() =>
      future.cancel()
      become(rec)
      sender ! new SimpleCommand("группа")
  }
}

case class CurseCommand(target: String) extends RenderableCommand {
  def render = s"кол !прок! $target"
}

class GroupStatusCommand extends RenderableCommand {
  def render = "группа"
}
