package com.tort.mudai.person

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.mapper.{LocationPersister, MoveEvent}
import scalaz._
import Scalaz._
import com.tort.mudai.event.StatusLineEvent

class Passages(persister: LocationPersister, person: ActorRef) extends Actor {
  def receive = rec(0)

  def rec(level: Int): Receive = {
    case StatusLineEvent(_, _, _, _, level, _) =>
      rec(level)
    case TriggeredMoveRequest("У шалаша", direction, "Тихий угол") =>
      sender ! new SimpleCommand(s"дать $level кун следопыт")
    case TriggeredMoveRequest("Тихий угол", direction, "У шалаша") =>
      sender ! new SimpleCommand(s"дать $level кун следопыт")
    case TriggeredMoveRequest("На лугу", direction, "На лугу") if direction == "v1_v2_trigger" =>
      sender ! new SimpleCommand(s"дать $level кун цыган")
      person ! MoveEvent(persister.loadLocation("a2487caf-444f-4736-978f-0f1fbbd6083d").some,
        direction.some,
        persister.loadLocation("ebb1d973-693f-4d3c-95f9-8b0f187f7eaa"))
    case TriggeredMoveRequest("На лугу", direction, "На лугу") if direction == "v2_v1_trigger" =>
      sender ! new SimpleCommand(s"дать $level кун цыган")
      person ! MoveEvent(persister.loadLocation("ebb1d973-693f-4d3c-95f9-8b0f187f7eaa").some,
        direction.some,
        persister.loadLocation("a2487caf-444f-4736-978f-0f1fbbd6083d"))
    case TriggeredMoveRequest("Дорога", direction, "Дорога") if direction == "trigger_swamp_south" =>
      sender ! new SimpleCommand(s"дать ${level * 2} кун болотник")
      person ! MoveEvent(persister.loadLocation("f6c1f708-c24c-41cc-b653-9ff36a64e731").some,
        direction.some,
        persister.loadLocation("9e0df04f-4bf2-4313-b305-fa84d574300b"))
    case TriggeredMoveRequest("Дорога", direction, "Дорога") if direction == "trigger_swamp_north" =>
      sender ! new SimpleCommand(s"дать ${level} кун болотник")
      person ! MoveEvent(persister.loadLocation("2b7f6585-69eb-4e9f-8d46-bb649b42ca36").some,
        direction.some,
        persister.loadLocation("fcb3a6b8-0393-423e-aae6-7c2335e2c3bc"))
  }
}
