package com.tort.mudai.person

import akka.actor.{Props, Actor}
import com.tort.mudai.event.{PasswordPromptEvent, LoginPromptEvent}
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.mapper.{PathTo, SQLLocationPersister, MudMapper}
import scalaz._

class Person(login: String, password: String) extends Actor {
  import context._

  val adapter = actorOf(Props[Adapter])
  val snoopable = actorOf(Props[Snoopable])
  val tasks = Seq(actorOf(Props(classOf[MudMapper], new SQLLocationPersister, new SQLLocationPersister)))

  def receive = {
    case snoop: Snoop => snoopable ! snoop
    case rawRead: RawRead => snoopable ! rawRead
    case rawWrite: RawWrite => adapter ! rawWrite
    case Login => adapter ! Login
    case Zap => adapter ! Zap
    case e: LoginPromptEvent => adapter ! new SimpleCommand(login)
    case e: PasswordPromptEvent => adapter ! new SimpleCommand(password)
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

