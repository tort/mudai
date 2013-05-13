package com.tort.mudai.person

import akka.actor.{ActorRef, Props, Actor}

class Person extends Actor {

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
      }
  }
}

case class Snoop(snooper: ActorRef)

case object StopSnoop

case class RawWrite(line: String)
