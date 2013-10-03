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
    case FightRoundEvent(state, target, targetState) =>
      val person = sender
      person ! RequestPulses
      become {
        case e: PeaceStatusEvent =>
          person ! YieldPulses
          unbecome()
        case DisarmEvent(_, weapon) =>
          sender ! new SimpleCommand("вз коп")
          sender ! new SimpleCommand("воор молод")
          sender ! new SimpleCommand("держ полов")
        case CurseFailedEvent() =>
          person ! new SimpleCommand("кол !прок! %s".format(target))
        case TargetAssistedEvent(target) =>
          sender ! new SimpleCommand("кол !прок! %s".format(target))
        case MemFinishedEvent() =>
          val person = sender
          person ! ReadyForFight
        case c: RenderableCommand if sender == antiBasher => person ! c
        case e => antiBasher ! e
      }
    case MemFinishedEvent() =>
      val person = sender
      person ! ReadyForFight
    case Attack(target) =>
      sender ! RequestPulses
      val person = sender
      person ! new SimpleCommand("кол !прок! %s".format(target))
    case KillEvent(target, exp) =>
      sender ! NeedMem
      sender ! YieldPulses
    case TargetFleeEvent(target, direction) =>
      sender ! RequestPulses
      sender ! RequestWalkCommand(direction)
      become {
        case MoveEvent(from, Some(direction), to) =>
          sender ! new SimpleCommand("уб %s".format(target))
          unbecome()
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
        case 2 =>
          sender ! new SimpleCommand("встать")
          become(rec)
        case r =>
          become(bashed(r))
      }
  }
}
