package com.tort.mudai.person

import akka.actor.Actor
import com.tort.mudai.event._
import com.tort.mudai.command.{RequestWalkCommand, WalkCommand, SimpleCommand}
import com.tort.mudai.event.TargetFleeEvent
import com.tort.mudai.event.FightRoundEvent
import com.tort.mudai.event.PeaceStatusEvent
import com.tort.mudai.event.MemFinishedEvent

class Fighter extends Actor {
  import context._

  def receive = {
    case FightRoundEvent(state, target, targetState) =>
      println("FIGHT STARTED")
      val person = sender
      person ! RequestPulses
      become {
        case e: PeaceStatusEvent =>
          println("FIGHT FINISHED")
          person ! YieldPulses
          unbecome()
      }
    case MemFinishedEvent() =>
      val person = sender
      person ! ReadyForFight
    case Attack(target) =>
      sender ! RequestPulses
      val person = sender
      person ! new SimpleCommand("кол !прок! %s".format(target))
    case TargetAssistedEvent(target) =>
      println("ASSISTED EVENT !%s!".format(target))
      sender ! new SimpleCommand("кол !прок! %s".format(target))
    case KillEvent(target, exp) =>
      sender ! NeedMem
      sender ! YieldPulses
    case TargetFleeEvent(target, direction) =>
      sender ! RequestPulses
      sender ! RequestWalkCommand(direction)
      sender ! new SimpleCommand("уб %s".format(target))
  }
}
