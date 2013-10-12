package com.tort.mudai.person

import akka.actor.{ActorRef, Props, Actor}
import com.tort.mudai.event._
import com.tort.mudai.command.{RenderableCommand, RequestWalkCommand, WalkCommand, SimpleCommand}
import com.tort.mudai.event.TargetFleeEvent
import com.tort.mudai.event.FightRoundEvent
import com.tort.mudai.event.PeaceStatusEvent
import com.tort.mudai.event.MemFinishedEvent
import com.tort.mudai.mapper.MoveEvent

class Fighter(person: ActorRef) extends Actor {

  import context._

  val antiBasher = actorOf(Props(classOf[AntiBasher]))

  def receive = {
    case MemFinishedEvent() =>
      val person = sender
      person ! new SimpleCommand("вст")
      person ! ReadyForFight
    case Attack(target) =>
      sender ! RequestPulses
      val person = sender
      person ! new SimpleCommand(s"прик все убить $target")
      person ! new SimpleCommand(s"кол !прок! $target")
      person ! new SimpleCommand("отд")
    case KillEvent(target, exp) =>
      sender ! YieldPulses
    case TargetFleeEvent(target, direction) =>
      become {
        case Pulse =>
          sender ! RequestWalkCommand(direction)
          become {
            case MoveEvent(from, Some(direction), to) =>
              person ! new SimpleCommand(s"прик все убить $target")
              unbecome()
              unbecome()
          }
      }

    case RequestPulses => person ! RequestPulses
    case YieldPulses => person ! YieldPulses
    case c: RenderableCommand if sender == antiBasher => person ! c
    case e => antiBasher ! e
  }
}

class AntiBasher extends Actor {

  import context._

  def receive = rec

  def rec: Receive = {
    case BashEvent(basher, target) =>
      become(bashed(0))
  }

  def bashed(round: Int): Receive = {
    case FightRoundEvent(state, target, targetState) =>
      (round + 1) match {
        case 1 =>
          sender ! new SimpleCommand("встать")
          become(rec)
        case r =>
          become(bashed(r))
      }
  }
}
