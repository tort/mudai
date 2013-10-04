package com.tort.mudai.person

import akka.actor.Actor
import com.tort.mudai.command.SimpleCommand

class VillageWellQuest extends Actor {
  def receive = {
    case TriggeredMoveRequest("У покосившегося сруба.", direction, "Дно колодца") =>
      sender ! new SimpleCommand("прыгнуть колодец")
  }
}


