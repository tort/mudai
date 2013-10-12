package com.tort.mudai.person

import akka.actor.{ActorRef, Props, Actor}
import com.tort.mudai.event._
import com.tort.mudai.command.{RenderableCommand, RequestWalkCommand, SimpleCommand}
import com.tort.mudai.event.TargetFleeEvent
import com.tort.mudai.event.FightRoundEvent
import com.tort.mudai.event.MemFinishedEvent
import com.tort.mudai.mapper.{Direction, LocationPersister, MoveEvent}
import scalaz.@@

class Fighter(person: ActorRef, persister: LocationPersister) extends Actor {

  import context._

  val antiBasher = actorOf(Props(classOf[AntiBasher]))

  def receive = rec
  def rec: Receive = {
    case MemFinishedEvent() =>
      val person = sender
      person ! new SimpleCommand("вст")
      person ! ReadyForFight
    case Attack(target) =>
      val person = sender
      person ! RequestPulses
      person ! new SimpleCommand(s"прик все убить $target")
      person ! new SimpleCommand(s"кол !прок! $target")
      person ! new SimpleCommand("отд")
    case KillEvent(target, exp) =>
      person ! new SimpleCommand("вст")
      person ! YieldPulses
    case TargetFleeEvent(target, direction) =>
      become(waitPulse(target, direction))
    case RequestPulses => person ! RequestPulses
    case YieldPulses => person ! YieldPulses
    case c: RenderableCommand if sender == antiBasher => person ! c
    case e => antiBasher ! e
  }

  def waitPulse(target: String, direction: String @@ Direction): Receive = {
    case Pulse =>
      person ! RequestWalkCommand(direction)
      become(waitMove(target))
  }

  def waitMove(target: String): Receive = {
    case MoveEvent(from, Some(direction), to) =>
      persister.mobByShortName(target).map(_.alias) match {
        case None => println(s"### UNKNOWN ALIAS FOR $target")
        case Some(alias) =>
          person ! new SimpleCommand(s"прик все убить ${alias}")
      }
    become(rec)
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
      round + 1 match {
        case 1 =>
          sender ! new SimpleCommand("встать")
          become(rec)
        case r =>
          become(bashed(r))
      }
  }
}
