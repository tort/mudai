package com.tort.mudai.person

import scalaz._
import Scalaz._
import scala.concurrent.duration._
import akka.actor.Actor
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event.OrderFailedEvent

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
      if (target.isFragging) {
        sender ! new SimpleCommand(s"кол !д о! $alias")
      }

    become(waitGroupEvent orElse rec)
  }

  def waitGroupEvent: Receive = {
    case OrderFailedEvent() =>
      system.scheduler.scheduleOnce(1 second, sender, new SimpleCommand("группа"))
      become(rec)
  }
}
