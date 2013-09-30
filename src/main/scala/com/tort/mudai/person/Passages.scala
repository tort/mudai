package com.tort.mudai.person

import akka.actor.Actor
import com.tort.mudai.command.SimpleCommand

class Passages extends Actor {
  def receive = {
    case TriggeredMoveRequest("У шалаша", direction, "Тихий угол") =>
      sender ! new SimpleCommand("дать 14 кун следопыт")
    case TriggeredMoveRequest("Тихий угол", direction, "У шалаша") =>
      sender ! new SimpleCommand("дать 14 кун следопыт")
  }
}

class VillageWellQuest extends Actor {
  def receive = {
    case TriggeredMoveRequest("У покосившегося сруба.", direction, "Дно колодца") =>
      sender ! new SimpleCommand("прыгнуть колодец")
  }
}
