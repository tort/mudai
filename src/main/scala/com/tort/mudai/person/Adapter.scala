package com.tort.mudai.person

import akka.actor.{Props, Actor}
import com.tort.mudai.telnet.MudConnector

class Adapter extends Actor {
  import context._
  val connector = new MudConnector

  def receive = {
    case Login =>
      val person = sender
      val channelFuture = connector.connect(self)
      become {
        case RawWrite(line) =>
          channelFuture.sync.getChannel.write(line)
        case rawRead: RawRead => person ! rawRead
        case Disconnect => unbecome()
        case Zap =>
          channelFuture.sync.getChannel.close
          unbecome()
      }
    case RawWrite(text) => sender ! RawRead("### not connected")
    case Zap => sender ! RawRead("### not connected")
  }
}

object Adapter {
  def props(connector: MudConnector) = Props(classOf[Adapter], connector)
}

case object Login
case object Disconnect
case object Zap

case class RawRead(text: String)
