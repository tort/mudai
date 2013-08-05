package com.tort.mudai.person

import akka.actor.{ActorRef, Props, Actor}
import com.tort.mudai.event.{PasswordPromptEvent, LoginPromptEvent}
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.mapper.{PathHelper, Location, SQLLocationPersister, MudMapper}
import com.tort.mudai.task.TravelTo
import scala.concurrent.duration._

class Person(login: String, password: String) extends Actor {

  import context._

  val adapter = actorOf(Props[Adapter])
  val snoopable = actorOf(Props[Snoopable])
  val persister = new SQLLocationPersister
  val pathHelper = new PathHelper(persister)
  val mapper = actorOf(Props(classOf[MudMapper], pathHelper, persister, persister))
  val coreTasks = Seq(mapper)


  def receive: Receive = rec(coreTasks)

  def rec(tasks: Seq[ActorRef]): Receive = {
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
      system.scheduler.schedule(0 millis, 1000 millis, travelTask, Pulse)
      travelTask ! e
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

case class Snoop(onRawRead: (String) => Unit)

case object StopSnoop

case object CurrentLocation

case class RawWrite(line: String)

case class GoTo(loc: Location)

case object Pulse
