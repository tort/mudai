package com.tort.mudai.person

import scalaz._
import Scalaz._
import scala.concurrent.duration._
import akka.actor.{ActorRef, Props, Actor}
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event._
import Spell._
import com.tort.mudai.mapper.Mob
import com.tort.mudai.event.OrderFailedEvent
import com.tort.mudai.event.SpellFailedEvent
import scala.Some

class TraderAttacker(person: ActorRef) extends Actor {

  import context._

  private val holder = context.actorOf(Props(classOf[Caster], LongHold, person))
  private val netter = context.actorOf(Props(classOf[Caster], Net, person))

  def receive = rec

  def rec: Receive = {
    case AttackByAlias(target) =>
      person ! new SimpleCommand(s"прик все убить $target")
    case Attack(target, number) =>
      val alias = number match {
        case None => s"${target.alias.get}"
        case Some(x) => s"${x}.${target.alias.get}"
      }
      person ! new SimpleCommand(s"прик все убить $alias")
      if (target.isFragging) {
        holder ! Attack(target, number)
      }
      if (target.canFlee) {
        netter ! Attack(target, number)
      }

      become(waitGroupEvent orElse rec)
    case e =>
      holder ! e
      netter ! e
  }

  def waitGroupEvent: Receive = {
    case OrderFailedEvent() =>
      system.scheduler.scheduleOnce(1 second, person, new SimpleCommand("группа"))
      become(rec)
  }
}

class Caster(spell: String @@ SpellName, person: ActorRef) extends Actor {

  def receive = rec

  def rec: Receive = {
    case Attack(target, number) =>
      castLongHold(target)
      context.become(failureTracker(target) orElse successTracker orElse rec)
  }

  def failureTracker(target: Mob): Receive = {
    case SpellFailedEvent(spell) =>
      castLongHold(target)
    case KillEvent(_, _, _, _) =>
      context.become(rec)
  }

  private def castLongHold(target: Mob) {
    person ! SpellCommand(target.alias.get, spell)
  }

  private def successTracker: Receive = {
    case SpellSucceededEvent(t, s) if s === spell =>
      context.become(rec)
  }
}
