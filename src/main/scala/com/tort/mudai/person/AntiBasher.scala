package com.tort.mudai.person

import akka.actor.Actor
import com.tort.mudai.event.{FightRoundEvent, BashEvent}
import com.tort.mudai.command.SimpleCommand

class AntiBasher extends Actor {
  import context._

  def receive = rec

  def rec: Receive = {
    case BashEvent(basher, target) =>
      become(bashed(0))
  }

  def bashed(round: Int): Receive = {
    case FightRoundEvent(state, target, targetState) =>
      round + 1 match {
        case 1 =>
          sender ! new SimpleCommand("встать")
          become(rec)
        case r =>
          become(bashed(r))
      }
  }
}
