package com.tort.mudai.person

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.mapper.{LocationPersister, MoveEvent}
import scalaz._
import Scalaz._

class Passages(persister: LocationPersister, person: ActorRef) extends Actor {
  val level = 18

  def receive = {
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
  }
}
