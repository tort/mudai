package com.tort.mudai.person

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.command.SimpleCommand

class OrdinaryAttacker(person: ActorRef) extends Actor {
  def receive = {
      case AttackByAlias(target) =>
      person ! new SimpleCommand(s"убить $target")
    case Attack(target, number) =>
      val alias = number match {
        case None => s"${target.alias.get}"
        case Some(x) => s"${x}.${target.alias.get}"
      }
      person ! new SimpleCommand(s"убить $alias")
  }
}
