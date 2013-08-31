package com.tort.mudai.person

import akka.actor.Actor

class Snoopable extends Actor {
  import context._

  def receive = {
    case Snoop(onRawRead) => become {
      case rawRead: RawRead => onRawRead(rawRead.text)
      case StopSnoop => unbecome()
    }
  }
}
