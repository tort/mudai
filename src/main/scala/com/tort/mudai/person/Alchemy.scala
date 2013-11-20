package com.tort.mudai.person

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.event.GlanceEvent
import com.tort.mudai.mapper.{Item, LocationPersister}
import scalaz._
import Scalaz._
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.ItemAndNumber

class Alchemy(person: ActorRef, persister: LocationPersister) extends Actor {
  def receive = {
    case GlanceEvent(room, _) =>
      val visibleIngridients = room.objectsPresent.toSet.map(toItem).filter(_.objectType === "ingridient".some)

      visibleIngridients.foreach(i => person ! new SimpleCommand(s"взять все.${i.alias}"))
  }

  private def toItem(ian: ItemAndNumber): Item = persister.itemByFullName(ian.item)
}
