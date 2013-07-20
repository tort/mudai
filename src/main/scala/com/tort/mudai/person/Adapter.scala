package com.tort.mudai.person

import akka.actor.{Props, Actor}
import com.tort.mudai.telnet.MudConnector
import com.tort.mudai.event._
import scalaz._
import Scalaz._
import org.jboss.netty.channel.Channel
import com.tort.mudai.command.RenderableCommand

class Adapter extends Actor {

  import context._

  val connector = new MudConnector
  val Encoding = "5"
  val adapterTriggers = Seq(
    new SimpleTrigger(".*^\\* В связи с проблемами перевода фразы ANYKEY нажмите ENTER.*", Array[String]("", "смотр")),
    new SimpleTrigger(".*^Select one : $", Array[String](Encoding))
  )
  val triggers: Seq[EventTrigger[Event]] = Seq(
    new LoginPromptTrigger,
    new PasswordPromptTrigger
  )

  private def parse(text: String) =
    adapterTriggers.filter(_.matches(text)).map(_.command).flatten

  private def parseEvents(text: String) =
    triggers.filter(_.matches(text)).map(_.fireEvent(text))

  private def fire(channel: Channel)(actions: Seq[String]) {
    actions.foreach(action => channel.write(action + "\n"))
  }

  def receive = {
    case Login =>
      val person = sender
      val channelFuture = connector.connect(self)
      become {
        case c: RenderableCommand =>
          channelFuture.sync.getChannel.write(c.render + "\n")
        case RawWrite(line) =>
          channelFuture.sync.getChannel.write(line)
        case rawRead@RawRead(text) =>
          text |> parse |> fire(channelFuture.sync().getChannel)
          parseEvents(text).foreach(person ! _)
          person ! rawRead
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
