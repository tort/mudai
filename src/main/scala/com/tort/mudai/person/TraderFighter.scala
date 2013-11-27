package com.tort.mudai.person

import akka.actor.{ActorRef, Props, Actor}
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.mapper._
import scalaz._
import Scalaz._
import com.tort.mudai.mapper.Mob.ShortName
import com.tort.mudai.event._
import scala.concurrent.duration._
import com.tort.mudai.event.KillEvent
import com.tort.mudai.event.GroupStatusEvent
import com.tort.mudai.event.DisarmAssistantEvent
import Spell._

class TraderFighter(person: ActorRef, persister: LocationPersister, mapper: ActorRef) extends Actor {

  import context._

  val antiBasher = actorOf(Props(classOf[AntiBasher]))
  val fleeker = actorOf(Props(classOf[Fleeker], mapper, persister))
  val attacker = actorOf(Props(classOf[TraderAttacker], person))

  def receive = rec(false)

  /*
   * Define universal criteria for battle start and finish
   */
  def rec(peaceStatus: Boolean): Receive = {
    case e@Attack(target, number) =>
      person ! RequestPulses
      antiBasher ! e
      if (sender != fleeker) fleeker ! e
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
    case TargetFleeEvent(_, _) =>
      person ! new GroupStatusCommand
    case DisarmAssistantEvent(_, _, _) =>
      person ! new SimpleCommand("взять чудс")
      person ! new SimpleCommand("дать чудс гриден")
      person ! new SimpleCommand("прик все вооруж чудс")
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
      val mob: Option[Mob] = persister.mobByShortNameSubstring(name)
      mob.flatMap(_.alias) foreach {
        case alias => person ! new SpellCommand(alias, CritHeal)
      }
    }
  }
}

case class SpellCommand(target: String, spell: String @@ SpellName) extends RenderableCommand {
  def render = s"кол !$spell! $target"
}

class GroupStatusCommand extends RenderableCommand {
  def render = "группа"
}

object Spell {
  trait SpellName

  val Curse = spell("проклятье")
  val CritHeal = spell("критическое исцеление")
  val LongHold = spell("длительное оцепенение")
  val Net = spell("сеть")

  def spell(s: String): String @@ SpellName = Tag(s)

  implicit val spellEqual: Equal[String @@ SpellName] = Equal.equal(_ == _)
}
