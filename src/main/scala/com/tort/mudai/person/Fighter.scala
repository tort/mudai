package com.tort.mudai.person

import akka.actor.Actor
import com.tort.mudai.event.{MemFinishedEvent, PeaceStatusEvent, FightRoundEvent}
import com.tort.mudai.command.SimpleCommand

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
          person ! NeedMem
          person ! YieldPulses
          unbecome()
      }
    case MemFinishedEvent() =>
      val person = sender
      person ! ReadyForFight
    case Attack(target) =>
      val person = sender
      person ! new SimpleCommand("кол !прок! %s".format(target))
  }
}
