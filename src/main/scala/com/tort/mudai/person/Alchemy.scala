package com.tort.mudai.person

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.event.GlanceEvent
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.mapper.LocationPersister

class Alchemy(person: ActorRef, persister: LocationPersister) extends Actor {
  val ingridients = Set()

  def receive = {
    case GlanceEvent(room, _) =>
      room.objectsPresent.toSet.intersect(ingridients).map(fn => persister.itemByFullName(fn)).foreach(person ! new SimpleCommand(s"взять все.$alias"))
  }
}
