package com.tort.mudai.person

import akka.actor.Actor
import com.tort.mudai.command.SimpleCommand

class Passages extends Actor {
  val level = 17

  def receive = {
    case TriggeredMoveRequest("У шалаша", direction, "Тихий угол") =>
      sender ! new SimpleCommand(s"дать $level кун следопыт")
    case TriggeredMoveRequest("Тихий угол", direction, "У шалаша") =>
      sender ! new SimpleCommand(s"дать $level кун следопыт")
  }
}
