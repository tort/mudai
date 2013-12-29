package com.tort.mudai.quest

import akka.actor.{Actor, ActorRef}
import com.tort.mudai.mapper.{PathHelper, LocationPersister}

class Prospection(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends Actor {
  def receive = dig

  def dig: Receive = PartialFunction.empty
}
