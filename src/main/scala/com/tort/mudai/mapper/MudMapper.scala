package com.tort.mudai.mapper

import com.tort.mudai.event.GlanceEvent
import com.tort.mudai.person.{RawRead, CurrentLocation}
import akka.actor.Actor
import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import scalaz._
import Scalaz._
import scalax.collection.mutable.Graph
import scalax.collection.edge.Implicits._
import scalax.collection.edge.LDiEdge
import com.tort.mudai.Metadata.Direction._

class MudMapper @Inject()(pathHelper: PathHelper, locationPersister: LocationPersister, transitionPersister: TransitionPersister) extends Actor {

  import context._
  import locationPersister._
  import transitionPersister._
  import pathHelper._

  def receive: Receive = rec(None)

  def rec(current: Option[Location]): Receive = {
    case CurrentLocation => sender ! current
    case GlanceEvent(room, None) =>
      become(rec(location(room)))
    case GlanceEvent(room, Some(direction)) =>
      val newLocation = location(room)
      transition(current, direction, newLocation)
      become(rec(newLocation))
    case PathTo(target) =>
      current.foreach(l => println(l.title))
      val path = pathTo(current, target)
      sender ! RawRead(path.map(_.id).mkString)
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

case class PathTo(target: Location)

class PathHelper(transitionPersister: TransitionPersister) {
  def pathTo(current: Option[Location], target: Location): List[Direction] = {
    current.flatMap(curr => pathTo(curr, target)).getOrElse(List())
  }

  private def pathTo(currentLocation: Location, target: Location): Option[List[Direction]] = {
    val graph = Graph.empty[String, LDiEdge]

   transitionPersister.allTransitions.foreach {
      case transition =>
        val edge = (transition.from.id ~+> transition.to.id)(transition.direction.id)
        graph += edge
    }

    def node(location: String) = graph.get(location)

    val shortest = node(currentLocation.id) shortestPathTo node(target.id)
    shortest.map(_.edges.map(_.label.toString).map(nameToDirection(_)))
  }
}
