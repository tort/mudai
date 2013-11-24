package com.tort.mudai.mapper

import com.tort.mudai.person._
import akka.actor.{Cancellable, Actor}
import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import com.tort.mudai.mapper.Direction._
import scalaz._
import Scalaz._
import com.tort.mudai.mapper.Zone.ZoneName
import com.tort.mudai.event._
import com.tort.mudai.person.TriggeredMoveRequest
import com.tort.mudai.person.FleeCommand
import scala.Some
import com.tort.mudai.event.GlanceEvent
import com.tort.mudai.command.RequestWalkCommand
import com.tort.mudai.command.WalkCommand
import com.tort.mudai.event.KillEvent
import com.tort.mudai.person.RawRead
import com.tort.mudai.event.TargetAssistedEvent
import com.tort.mudai.event.FleeEvent
import scala.concurrent.duration._
import Mob._

class MudMapper @Inject()(pathHelper: PathHelper, locationPersister: LocationPersister, transitionPersister: TransitionPersister)
  extends Actor with ReachabilityHelper {

  import context._
  import locationPersister._
  import transitionPersister._
  import pathHelper._

  def receive: Receive = rec(None, None, None)

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

  private def waitMoveHint: Receive = {
    case MoveEvent(from, dir, to) =>
      become(rec(from, dir.some, to.some))
  }

  private def waitFleeHint(future: Cancellable, from: Option[Location], direction: String @@ Direction, to: Option[Location]): Receive = {
    case FleeEvent() =>
      become(rec(from, direction.some, to))
      future.cancel()
    case FleeFailed(prev, dir, curr) =>
      become(rec(prev, dir, curr))
  }

  def rec(previousLocation: Option[Location], previousDirection: Option[String @@ Direction], currentLocation: Option[Location]): Receive = {
    case LastDirection =>
      sender ! previousDirection
    case FleeCommand(direction) =>
      val fleeTo = locationPersister.loadLocation(currentLocation.get, direction)
      val future = system.scheduler.scheduleOnce(2 seconds, self, FleeFailed(previousLocation, previousDirection, currentLocation))
      become(waitFleeHint(future, currentLocation, direction, fleeTo))
    case RequestWalkCommand(direction) =>
      currentLocation.foreach {
        case current =>
          loadTransition(current, direction) match {
            case Some(t) if !t.isTriggered =>
              sender ! WalkCommand(direction)
            case Some(transition) if transition.isTriggered =>
              sender ! TriggeredMoveRequest(current.title, direction, transition.to.title)
              become(waitMoveHint)
            case _ =>
              println(s"### NO WAY from ${current.title} on $direction")
          }
      }
    case CurrentLocation => sender ! currentLocation
    case GlanceEvent(room, None) =>
      //TODO fix case when recall to non-unique room.
      location(room).foreach {
        case newCurrentLocation if newCurrentLocation.some /== currentLocation =>
          val mobs = extractMobs(room, newCurrentLocation.zone)
          updateHabitation(mobs, newCurrentLocation.some)
          updateItemAndArea(room, newCurrentLocation.some)
          become(rec(currentLocation, None, newCurrentLocation.some))
          sender ! MobViewEvent(mobs)
        case _ =>
          val mobs = extractMobs(room, currentLocation.flatMap(_.zone))
          sender ! MobViewEvent(mobs)
      }
    case GlanceEvent(room, Some(direction)) =>
      locationFromMap(currentLocation, direction) match {
        case None =>
          val loc = loadLocation(room) match {
            case Nil =>
              val newLoc = saveLocation(room)
              transition(currentLocation, direction, newLoc.some, room)
              newLoc
            case locations =>
              findAmongKnownRooms(locations, currentLocation, direction) match {
                case l@Some(x) =>
                  transition(currentLocation, direction, l, room)
                  x
                case None =>
                  val newLoc = saveLocation(room)
                  transition(currentLocation, direction, newLoc.some, room)
                  newLoc
              }
          }

          val mobs = extractMobs(room, loc.zone)
          updateHabitation(mobs, loc.some)
          updateItemAndArea(room, loc.some)
          become(rec(currentLocation, direction.some, loc.some))
          sender ! MoveEvent(currentLocation, direction, loc)
          sender ! MobViewEvent(mobs)
        case Some(loc) =>
          if (loc.title == room.title && isAlternativeDescription(loc, room.desc)){
            println(s"### INTERSECTION ${worstIntersection(loc, room.desc)}")
            locationPersister.addDescription(loc, room.desc)
          }
          val mobs = extractMobs(room, loc.zone)
          updateHabitation(mobs, loc.some)
          updateItemAndArea(room, loc.some)
          become(rec(currentLocation, direction.some, loc.some))
          sender ! MoveEvent(currentLocation, direction, loc)
          sender ! MobViewEvent(mobs)
      }
    case PathTo(target) =>
      val path = pathTo(currentLocation, target)
      sender ! RawRead(path.mkString)
    case KillEvent(shortName, exp, genitive, _) =>
      for {
        mob <- locationPersister.mobByShortName(shortName)
        if mob.genitive.isEmpty
      } yield updateGenitive(mob, genitive)
    case TargetAssistedEvent(assist, _) =>
      for {
        mob <- locationPersister.mobByShortName(assist)
        if !mob.isAssisting
      } yield locationPersister.markAsAssisting(mob)
    case TargetFleeEvent(target, direction) =>
      for {
        mob <- mobByShortName(target)
        if !mob.canFlee
      } yield markAsFleeker(mob)
    case FightRoundEvent(_, target, _) =>
      for {
        m <- mobByShortName(target)
        if !m.isAgressive
      } yield markAsAgressive(m)
    case NameZone(zoneName, initLocation) =>
      val zone = zoneByName(zoneName)
      initLocation.orElse(currentLocation).foreach(l => updateZone(l, zone))
    case CheckUnreachable =>
      checkUnreachable.foreach(x => println(s"${x.id}"))
  }

  private def isAlternativeDescription(location: Location, visibleDesc: String): Boolean =
    unknownDescription(location, visibleDesc) && worstIntersection(location, visibleDesc) > 0.8


  private def unknownDescription(loc: Location, visibleDesc: String): Boolean = {
    !locationPersister.descriptions(loc.id).contains(visibleDesc)
  }

  private def worstIntersection(location: Location, desc: String): Float = {
    locationPersister.descriptions(location.id).map(intersection(_, desc)).min
  }

  private def intersection(left: String, right: String): Float = {
    right.toCharArray.intersect(left.toCharArray).length.toFloat / largest(left, right).length.toFloat
  }

  private def largest(left: String, right: String): String = Seq(left, right).maxBy(_.length)

  def extractMobs(room: RoomSnapshot, zone: Option[Zone]): Seq[Mob] = {
    room.mobs.map(_.trim).map(mobString =>
      tryRecognizeByFullName(mobString).orElse(tryRecognizeByShortname(mobString, zone)).getOrElse(persistMob(fullName(mobString)))
    )
  }

  private def tryRecognizeByFullName(mobString: String): Option[Mob] = {
    locationPersister.mobByFullName(fullName(mobString))
  }

  private val postfixes = Set(
    " стоит здесь.",
    " сидит здесь.",
    " отдыхает здесь.",
    " лежит здесь, без сознания."
  )

  private def tryRecognizeByShortname(mobString: String, zone: Option[Zone]): Option[Mob] = {
    for {
      postfix <- postfixes.find(p => mobString.endsWith(p))
      z <- zone
      mob <- locationPersister.allMobsByShortName(shortName(mobString.dropRight(postfix.length)), z)
    } yield mob
  }

  def checkUnreachable: Set[Location] = {
    val unique = locationPersister.locationByTitle("Изба деда и бабки").headOption
    locationPersister.allLocations.map {
      case l =>
        try {
          l -> pathHelper.pathTo(unique, l)
        } catch {
          case x: Throwable => l -> Nil
        }
    }.filterNot(x => x._2.size > 0).map(_._1).toSet
  }

  def checkZoneChange(zone: Option[Zone], newCurrent: Option[Location]) {
    for {
      prevZone <- zone
      newZone <- newCurrent.flatMap(_.zone)
      zoneLocation <- loadLocations(prevZone).headOption
    } yield updateZone(zoneLocation, prevZone)
  }

  private def updateZone(location: Location, zone: Zone) {
    reachableFrom(location, nonBorderNeighbors).foreach(updateLocation(zone.id))
  }

  private def updateHabitation(mobs: Seq[Mob], current: Option[Location]) {
    for {
      mob <- mobs
      loc <- current
    } yield persistHabitation(mob, loc)
  }

  private def updateItemAndArea(room: RoomSnapshot, current: Option[Location]) {
    for {
      itemAndNumber <- room.objectsPresent
      loc <- current
      if !(itemAndNumber.item.startsWith("Труп ") && itemAndNumber.item.endsWith(" лежит здесь."))
    } yield persistItemAndArea(itemAndNumber.item, loc)
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

case class MoveEvent(from: Option[Location], direction: String @@ Direction, to: Location)

case object CheckUnreachable

case class FleeFailed(from: Option[Location], direction: Option[String @@ Direction], to: Option[Location])

case class MobViewEvent(mobs: Seq[Mob])
