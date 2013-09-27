package com.tort.mudai.mapper

import com.tort.mudai.event.{KillEvent, GlanceEvent}
import com.tort.mudai.person.{TriggeredMoveRequest, RawRead, CurrentLocation}
import akka.actor.{ActorRef, Actor}
import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import com.tort.mudai.mapper.Direction._
import scalaz._
import Scalaz._
import com.tort.mudai.command.{WalkCommand, RequestWalkCommand}


class MudMapper @Inject()(pathHelper: PathHelper, locationPersister: LocationPersister, transitionPersister: TransitionPersister)
  extends Actor {

  import context._
  import locationPersister._
  import transitionPersister._
  import pathHelper._

  def receive: Receive = rec(None, None)

  def findAmongKnownRooms(locs: Seq[Location], previous: Option[Location], direction: String @@ Direction): Option[Location] = {
    locs.filter {
      case loc =>
        val path = oppositeDirection(direction) :: pathTo(previous, loc)
        val souths = path.filter(_ === South).size
        val norths = path.filter(_ === North).size
        val wests = path.filter(_ === West).size
        val easts = path.filter(_ === East).size

        souths === norths && wests === easts
    } match {
      case loc :: Nil => loc.some
      case _ => None
    }
  }

  def rec(previous: Option[Location], previousZone: Option[Zone]): Receive = {
    case RequestWalkCommand(direction) =>
      previous.foreach {
        case current =>
          loadTransition(current, direction) match {
            case Some(t) if !t.isTriggered =>
              sender ! WalkCommand(direction)
            case Some(transition) if transition.isTriggered =>
              sender ! TriggeredMoveRequest(current.title, direction, transition.to.title)
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
          checkZoneChange(previousZone, newCurrentLocation.some)
          become(rec(newCurrentLocation.some, newCurrentLocation.zone.orElse(previousZone)))
      }
    case GlanceEvent(room, Some(direction)) =>
      locationFromMap(previous, direction) match {
        case None =>
          val loc = loadLocation(room) match {
            case Nil =>
              val newLoc = saveLocation(room).some
              transition(previous, direction, newLoc, room)
              newLoc
            case locs =>
              findAmongKnownRooms(locs, previous, direction) match {
                case l@Some(x) =>
                  transition(previous, direction, l, room)
                  l
                case None =>
                  val newLoc = saveLocation(room).some
                  transition(previous, direction, newLoc, room)
                  newLoc
              }
          }

          updateMobAndArea(room, loc)
          updateItemAndArea(room, loc)
          become(rec(loc, previousZone))
        case Some(loc) =>
          updateMobAndArea(room, loc.some)
          updateItemAndArea(room, loc.some)
          checkZoneChange(previousZone, loc.some)
          become(rec(loc.some, loc.zone.orElse(previousZone)))
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
    } yield loadTransition(prev, direction, curr).getOrElse(saveTransition(prev, direction, curr, room, isWeak))
  }
}

case class PathTo(target: Location)

case class NameZone(zoneName: String, initLocation: Option[Location] = None)
