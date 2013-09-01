package com.tort.mudai.mapper

import com.tort.mudai.event.{KillEvent, GlanceEvent}
import com.tort.mudai.person.{RawRead, CurrentLocation}
import akka.actor.Actor
import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import com.tort.mudai.Metadata.Direction._
import scalaz._
import Scalaz._


class MudMapper @Inject()(pathHelper: PathHelper, locationPersister: LocationPersister, transitionPersister: TransitionPersister)
  extends Actor {

  import context._
  import locationPersister._
  import transitionPersister._
  import pathHelper._

  def receive: Receive = rec(None, None)

  def replaceUnstableChain(current: Option[Location]): Option[Location] = {
    weakChainIntersection match {
      case xs if (xs.length >= 3) =>
        val intersectionWeakIds = xs.map(_._1.id).toSet
        val weakToStrongLoc = xs.flatMap {
          case (tw, t) => tw.from -> t.from :: tw.to -> t.to :: Nil
        }.toSet.toMap
        val weakToStrongLocIds = weakToStrongLoc.map(x => x._1.id -> x._2.id)
        val forUpdateTo: Seq[Transition] = allWeakTransitions
          .filterNot(tw => intersectionWeakIds.contains(tw.id))
          .filter(tw => weakToStrongLocIds.contains(tw.to.id))
        forUpdateTo.foreach(tw => updateToTransition(tw.id, weakToStrongLocIds(tw.to.id)))
        val forUpdateFrom: Seq[Transition] = allWeakTransitions
          .filterNot(tw => intersectionWeakIds.contains(tw.id))
          .filter(tw => weakToStrongLocIds.contains(tw.from.id))
        forUpdateFrom.foreach(tw => updateFromTransition(tw.id, weakToStrongLocIds(tw.from.id)))

        deleteWeakIntersection(weakToStrongLoc.keys, xs.map(_._1))
        replaceWeakWithStrong
        current.map(loc => loadLocation(weakToStrongLocIds(loc.id)))
      case _ => None
    }
  }

  def findAmongKnownRooms(locs: Seq[Location], previous: Option[Location], direction: Direction): Option[Location] = {
    locs.filter {
      case loc =>
        val path = oppositeDirection(direction) :: pathTo(previous, loc)
        val souths = path.filter(_ == South).size
        val norths = path.filter(_ == North).size
        val wests = path.filter(_ == West).size
        val easts = path.filter(_ == East).size

        souths == norths && wests == easts
    } match {
      case loc :: Nil => loc.some
      case _ => None
    }
  }

  def rec(previous: Option[Location], previousZone: Option[Zone]): Receive = {
    case CurrentLocation => sender ! previous
    case GlanceEvent(room, None) =>
      //TODO fix case when recall to non-unique room.
      location(room).foreach {
        case newCurrentLocation =>
          updateMobAndArea(room, newCurrentLocation.some)
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
              replaceWeakWithStrong
              newLoc
            case locs =>
              findAmongKnownRooms(locs, previous, direction).orElse {
                val newLoc = saveLocation(room).some
                transition(previous, direction, newLoc, room)
                newLoc
              }
          }

          updateMobAndArea(room, loc)
          become(rec(loc, previousZone))
        case Some(loc) =>
          updateMobAndArea(room, loc.some)
          checkZoneChange(previousZone, loc.some)
          become(rec(loc.some, loc.zone.orElse(previousZone)))
      }
    case PathTo(target) =>
      val path = pathTo(previous, target)
      sender ! RawRead(path.map(_.id).mkString)
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
      ((neighbors.map(_.id) -- visited).flatMap(internal(visited ++ neighbors.map(_.id) + loc))) + loc
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

  private def location(room: RoomSnapshot) = loadLocation(room) match {
    case Nil => saveLocation(room).some
    case loc :: Nil => loc.some
    case _ => none
  }

  private def locationFromMap(currentOption: Option[Location], direction: Direction): Option[Location] = {
    currentOption.flatMap(current => loadLocation(current, direction))
  }

  private def transition(prevOption: Option[Location], direction: Direction, newOption: Option[Location], room: RoomSnapshot, isWeak: Boolean = false) {
    for {
      prev <- prevOption
      curr <- newOption
    } yield loadTransition(prev, direction, curr).getOrElse(saveTransition(prev, direction, curr, room, isWeak))
  }
}

case class PathTo(target: Location)

case class NameZone(zoneName: String, initLocation: Option[Location] = None)
