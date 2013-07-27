package com.tort.mudai.mapper

import com.tort.mudai.event.GlanceEvent
import com.tort.mudai.person.CurrentLocation
import akka.actor.Actor
import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import scalaz._
import Scalaz._

class MudMapper @Inject()(locationPersister: LocationPersister, transitionPersister: TransitionPersister) extends Actor {

  import context._
  import locationPersister._
  import transitionPersister._

  def receive: Receive = rec(None)

  def rec(current: Option[Location]): Receive = {
    case CurrentLocation => sender ! current
    case GlanceEvent(room, None) =>
      become(rec(location(room)))
    case GlanceEvent(room, Some(direction)) =>
      val newLocation = location(room)
      transition(current, direction, newLocation)
      become(rec(newLocation))
  }

  private def location(room: RoomSnapshot) = loadLocation(room) match {
    case Nil => saveLocation(room).some
    case loc :: Nil => loc.some
    case _ => none
  }

  private def transition(prevOption: Option[Location], direction: Direction, newOption: Option[Location]) {
    for {
      prev <- prevOption
      curr <- newOption
    } yield loadTransition(prev, direction, curr).getOrElse(saveTransition(prev, direction, curr))
  }
}


