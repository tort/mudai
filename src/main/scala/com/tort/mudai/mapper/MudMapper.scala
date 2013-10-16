package com.tort.mudai.mapper

import com.tort.mudai.event.{KillEvent, GlanceEvent}
import com.tort.mudai.person.{TriggeredMoveRequest, RawRead, CurrentLocation}
import akka.actor.Actor
import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import com.tort.mudai.mapper.Direction._
import scalaz._
import Scalaz._
import com.tort.mudai.command.{WalkCommand, RequestWalkCommand}
import com.tort.mudai.mapper.Zone.ZoneName


class MudMapper @Inject()(pathHelper: PathHelper, locationPersister: LocationPersister, transitionPersister: TransitionPersister)
  extends Actor {

  import context._
  import locationPersister._
  import transitionPersister._
  import pathHelper._

  def receive: Receive = rec(None)

  def findAmongKnownRooms(locations: Seq[Location], previous: Option[Location], direction: String @@ Direction): Option[Location] = {
    locations.filter {
      case loc =>
        val path = oppositeDirection(direction) :: pathTo(previous, loc)
        val souths = path.count(_ === South)
        val norths = path.count(_ === North)
        val wests = path.count(_ === West)
        val easts = path.count(_ === East)
        val ups = path.count(_ === Up)
        val downs = path.count(_ === Down)

        souths === norths && wests === easts && ups === downs
    } match {
      case loc :: Nil => loc.some
      case _ => None
    }
  }

  def rec(previous: Option[Location]): Receive = {
    case RequestWalkCommand(direction) =>
      previous.foreach {
        case current =>
          loadTransition(current, direction) match {
            case Some(t) if !t.isTriggered =>
              sender ! WalkCommand(direction)
            case Some(transition) if transition.isTriggered =>
              sender ! TriggeredMoveRequest(current.title, direction, transition.to.title)
              become(rec(transition.to.some))
            case _ =>
              println("### NO WAY THERE")
          }
      }
    case CurrentLocation => sender ! previous
    case GlanceEvent(room, None) =>
      //TODO fix case when recall to non-unique room.
      location(room).foreach {
        case newCurrentLocation =>
          updateMobAndArea(room, newCurrentLocation.some)
          updateItemAndArea(room, newCurrentLocation.some)
          become(rec(newCurrentLocation.some))

          previous foreach {
            case p =>
              if (p /== newCurrentLocation)
                sender ! MoveEvent(previous, None, newCurrentLocation)
          }
      }
    case GlanceEvent(room, Some(direction)) =>
      locationFromMap(previous, direction) match {
        case None =>
          val loc = loadLocation(room) match {
            case Nil =>
              val newLoc = saveLocation(room)
              transition(previous, direction, newLoc.some, room)
              newLoc
            case locations =>
              findAmongKnownRooms(locations, previous, direction) match {
                case l@Some(x) =>
                  transition(previous, direction, l, room)
                  x
                case None =>
                  val newLoc = saveLocation(room)
                  transition(previous, direction, newLoc.some, room)
                  newLoc
              }
          }

          updateMobAndArea(room, loc.some)
          updateItemAndArea(room, loc.some)
          become(rec(loc.some))
          sender ! MoveEvent(previous, direction.some, loc)
        case Some(loc) =>
          updateMobAndArea(room, loc.some)
          updateItemAndArea(room, loc.some)
          become(rec(loc.some))
          sender ! MoveEvent(previous, direction.some, loc)
      }
    case PathTo(target) =>
      val path = pathTo(previous, target)
      sender ! RawRead(path.mkString)
    case KillEvent(shortName, exp) =>
      makeKillable(shortName)
    case NameZone(zoneName, initLocation) =>
      val zone = zoneByName(zoneName)
      initLocation.orElse(previous).foreach {
        case location =>
          updateZone(location, zone)
      }
  }


  def checkZoneChange(zone: Option[Zone], newCurrent: Option[Location]) {
    for {
      prevZone <- zone
      newZone <- newCurrent.flatMap(_.zone)
      zoneLocation <- loadLocations(prevZone).headOption
    } yield updateZone(zoneLocation, prevZone)
  }

  private def updateZone(location: Location, zone: Zone) {
    zoneLocations(location).foreach(updateLocation(zone.id))
  }

  def zoneLocations(l: Location): Set[String] = {
    def internal(visited: Set[String])(loc: String): Set[String] = {
      val neighbors: Set[Location] = nonBorderNeighbors(loc)
      (neighbors.map(_.id) -- visited).flatMap(internal(visited ++ neighbors.map(_.id) + loc)) + loc
    }

    internal(Set())(l.id)
  }

  private def updateMobAndArea(room: RoomSnapshot, current: Option[Location]) {
    for {
      mob <- room.mobs
      loc <- current
      if !mob.contains("сражается")
    } yield persistMobAndArea(mob, loc)
  }

  private def updateItemAndArea(room: RoomSnapshot, current: Option[Location]) {
    for {
      item <- room.objectsPresent
      loc <- current
    } yield persistItemAndArea(item, loc)
  }

  private def location(room: RoomSnapshot) = loadLocation(room) match {
    case Nil => saveLocation(room).some
    case loc :: Nil => loc.some
    case _ => none
  }

  private def locationFromMap(currentOption: Option[Location], direction: String @@ Direction): Option[Location] = {
    currentOption.flatMap(current => loadLocation(current, direction))
  }

  private def transition(prevOption: Option[Location], direction: String @@ Direction, newOption: Option[Location], room: RoomSnapshot, isWeak: Boolean = false) {
    for {
      prev <- prevOption
      curr <- newOption
    } yield loadTransition(prev, direction, curr).getOrElse(saveTransition(prev, direction, curr, room))
  }
}

case class PathTo(target: Location)

case class NameZone(zoneName: String @@ ZoneName, initLocation: Option[Location] = None)

case class MoveEvent(from: Option[Location], direction: Option[String @@ Direction], to: Location)
