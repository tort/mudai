package com.tort.mudai.person

import scalaz._
import Scalaz._
import scala.concurrent.duration._
import akka.actor.{ActorRef, Props, Actor}
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.event.{LongHoldSucceededEvent, SpellFailedEvent, CurseSucceededEvent, OrderFailedEvent}
import Spell._
import com.tort.mudai.mapper.Mob

class Attacker(person: ActorRef) extends Actor {

  import context._

  private val holder = context.actorOf(Props(classOf[Holder], person))

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

      become(waitGroupEvent orElse rec)
    case e => holder ! e
  }

  def waitGroupEvent: Receive = {
    case OrderFailedEvent() =>
      system.scheduler.scheduleOnce(1 second, person, new SimpleCommand("группа"))
      become(rec)
  }
}

class Holder(person: ActorRef) extends Actor {
  def receive = rec

  def rec: Receive = {
    case Attack(target, number) =>
      castLongHold(target)
      context.become(failureTracker(target) orElse successTracker orElse rec)
  }

  def failureTracker(target: Mob): Receive = {
    case SpellFailedEvent(LongHold) =>
      castLongHold(target)
  }

  private def castLongHold(target: Mob) {
    person ! SpellCommand(target.alias.get, LongHold)
  }

  private def successTracker: Receive = {
    case LongHoldSucceededEvent(_) =>
      context.become(rec)
  }
}

class Curser extends Actor {

  import context._

  def receive = {
    case Attack(target, number) =>
      sender ! SpellCommand(target.alias.get, Curse)
      become {
        case SpellFailedEvent(Curse) =>
          sender ! SpellCommand(target.alias.get, Curse)
        case CurseSucceededEvent(_) =>
          unbecome()
      }
  }
}

