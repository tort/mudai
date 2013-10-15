package com.tort.mudai.person

import akka.actor.{ActorRef, Props, Actor}
import com.tort.mudai.event._
import com.tort.mudai.command.{RenderableCommand, RequestWalkCommand, SimpleCommand}
import com.tort.mudai.event.TargetFleeEvent
import com.tort.mudai.event.MemFinishedEvent
import com.tort.mudai.mapper.{Mob, Direction, LocationPersister, MoveEvent}
import scalaz._
import Scalaz._
import com.tort.mudai.mapper.Mob.ShortName

class Fighter(person: ActorRef, persister: LocationPersister) extends Actor {

  import context._

  val antiBasher = actorOf(Props(classOf[AntiBasher]))
  val curser = actorOf(Props(classOf[Curser]))

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
      person ! CurseCommand(target)
      person ! new SimpleCommand("отд")
    case KillEvent(target, exp) =>
      person ! new SimpleCommand("группа")
      become(waitGroupStatus)
    case TargetFleeEvent(target, direction) =>
      become(waitPulse(target, direction))
    case DisarmAssistantEvent(_, _, _) =>
      person ! new SimpleCommand("взять клевец")
      person ! new SimpleCommand("дать клевец дружинник")
      person ! new SimpleCommand("прик все воор клевец")
    case RequestPulses => person ! RequestPulses
    case YieldPulses => person ! YieldPulses
    case c: RenderableCommand if sender == antiBasher => person ! c
    case c: RenderableCommand if sender == curser => person ! c
    case e =>
      antiBasher ! e
      curser ! e
  }

  def waitGroupStatus: Receive = {
    case GroupStatusEvent(name, health, _, "Стоит") =>
      person ! new SimpleCommand("вст")

      if (health === "Ранен" || health.trim === "Лег.ранен") {
        val mob: Option[Mob] = persister.mobBy(name)
        mob.flatMap(_.alias) foreach {
          case alias => person ! new SimpleCommand(s"кол !к и! ${alias}")
        }
      }

      person ! YieldPulses
      become(rec)
    case GroupStatusEvent(_, _, _, status) =>
      println(status)
      become(rec)
  }

  def waitPulse(target: String @@ ShortName, direction: String @@ Direction): Receive = {
    case Pulse =>
      person ! RequestWalkCommand(direction)
      become(waitMove(target))
  }

  def waitMove(target: String @@ ShortName): Receive = {
    case MoveEvent(from, Some(direction), to) =>
      persister.mobBy(target).flatMap(_.alias) match {
        case None => println(s"### UNKNOWN ALIAS FOR $target")
        case Some(alias) =>
          person ! new SimpleCommand(s"прик все убить ${alias}")
      }
      become(rec)
  }
}

class Curser extends Actor {

  import context._

  def receive = {
    case Attack(target) =>
      become {
        case CurseFailedEvent =>
          sender ! CurseCommand(target)
        case CurseSucceededEvent(_) =>
          unbecome()
      }
  }
}

case class CurseCommand(target: String) extends RenderableCommand {
  def render = s"кол !прок! $target"
}
