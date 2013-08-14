package com.tort.mudai.person

import akka.actor._
import com.tort.mudai.event.{KillEvent, FightRoundEvent, PasswordPromptEvent, LoginPromptEvent}
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.mapper.{PathHelper, Location, SQLLocationPersister, MudMapper}
import com.tort.mudai.task.TravelTo
import scala.concurrent.duration._
import akka.actor.Terminated

class Person(login: String, password: String) extends Actor {

  import context._

  val adapter = actorOf(Props[Adapter])
  val snoopable = actorOf(Props[Snoopable])
  val persister = new SQLLocationPersister
  val pathHelper = new PathHelper(persister)
  val mapper = actorOf(Props(classOf[MudMapper], pathHelper, persister, persister))
  val fighter = actorOf(Props(classOf[Fighter]))
  val coreTasks = Seq(fighter, mapper)

  system.scheduler.schedule(0 millis, 500 millis, self, Pulse)

  def receive: Receive = rec(coreTasks, Nil)

  def rec(tasks: Seq[ActorRef], pulseRequests: Seq[ActorRef]): Receive = {
    case snoop: Snoop => snoopable ! snoop
    case rawRead: RawRead => snoopable ! rawRead
    case rawWrite: RawWrite => adapter ! rawWrite
    case Login => adapter ! Login
    case Zap => adapter ! Zap
    case e: LoginPromptEvent => adapter ! new SimpleCommand(login)
    case e: PasswordPromptEvent => adapter ! new SimpleCommand(password)
    case c: RenderableCommand => adapter ! c
    case e@GoTo(loc) =>
      val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper))
      become(rec(travelTask +: tasks, travelTask +: pulseRequests))
      watch(travelTask)
      travelTask ! e
    case RequestPulses =>
      become(rec(tasks, sender +: pulseRequests))
    case YieldPulses =>
      become(rec(tasks, pulseRequests.filterNot(_ == sender)))
    case Terminated(ref) =>
      become(rec(tasks.filterNot(_ == ref), pulseRequests.filterNot(_ == ref)))
      snoopable ! RawRead("### terminated " + ref.getClass.getName)
    case Pulse => pulseRequests.headOption.map(_ ! Pulse)
    case e => tasks.filter(_ != sender).foreach(_ ! e)
  }
}

class Snoopable extends Actor {

  import context._

  def receive = {
    case Snoop(onRawRead) => become {
      case rawRead: RawRead => onRawRead(rawRead.text)
      case StopSnoop => unbecome()
    }
  }
}

class Fighter extends Actor {

  import context._

  def receive = {
    case FightRoundEvent(state, target, targetState) =>
      println("FIGHT STARTED")
      val person = sender
      person ! RequestPulses
      become {
        case KillEvent(target, exp) =>
          println("FIGHT FINISHED")
          person ! YieldPulses
          unbecome()
      }
  }
}

case class Snoop(onRawRead: (String) => Unit)

case object StopSnoop

case object CurrentLocation

case class RawWrite(line: String)

case class GoTo(loc: Location)

case object Pulse

case object RequestPulses

case object YieldPulses
