package com.tort.mudai.person

import akka.actor.{ActorRef, Props, Actor}
import com.tort.mudai.event.{PasswordPromptEvent, LoginPromptEvent}
import com.tort.mudai.command.SimpleCommand

class Person(login: String, password: String) extends Actor {

  import context._

  val adapter = actorOf(Props[Adapter])

  def receive = {
    case Snoop(snooper) =>
      become {
        case rawRead: RawRead => snooper ! rawRead
        case StopSnoop => unbecome
        case rawWrite: RawWrite => adapter ! rawWrite
        case Login => adapter ! Login
        case Zap => adapter ! Zap
        case e: LoginPromptEvent => adapter ! new SimpleCommand(login)
        case e: PasswordPromptEvent => adapter ! new SimpleCommand(password)
      }
  }
}

case class Snoop(snooper: ActorRef)

case object StopSnoop

case class RawWrite(line: String)
