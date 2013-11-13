package com.tort.mudai.person

import scalaz._
import Scalaz._
import scala.concurrent.duration._
import akka.actor.{Props, Actor}
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event.{LongHoldSucceededEvent, SpellFailedEvent, CurseSucceededEvent, OrderFailedEvent}
import Spell._
import com.tort.mudai.mapper.Mob

class Attacker extends Actor {

  import context._

  private val curser = context.actorOf(Props(classOf[Holder]))

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
      if (target.isFragging) {
        curser ! Attack(target, number)
      }

      become(waitGroupEvent orElse rec)
    case e => curser ! e
  }

  def waitGroupEvent: Receive = {
    case OrderFailedEvent() =>
      system.scheduler.scheduleOnce(1 second, sender, new SimpleCommand("группа"))
      become(rec)
  }
}

class Holder extends Actor {
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
    sender ! SpellCommand(target.alias.get, LongHold)
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

