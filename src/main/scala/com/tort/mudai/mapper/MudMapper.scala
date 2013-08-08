package com.tort.mudai.mapper

import com.tort.mudai.event.GlanceEvent
import com.tort.mudai.person.{RawRead, CurrentLocation}
import akka.actor.Actor
import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import scalaz._
import Scalaz._


class MudMapper @Inject()(pathHelper: PathHelper, locationPersister: LocationPersister, transitionPersister: TransitionPersister)
  extends Actor {

  import context._
  import locationPersister._
  import transitionPersister._
  import pathHelper._

  def receive: Receive = rec(None)

  def replaceUnstableChain(current: Option[Location]): Option[Location] = {
    weakChainIntersection match {
      case xs if (xs.length >= 2) =>
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

  def rec(current: Option[Location]): Receive = {
    case CurrentLocation => sender ! current
    case GlanceEvent(room, None) =>
      become(rec(location(room)))
    case GlanceEvent(room, Some(direction)) =>
      val newCurrent: Option[Location] = replaceUnstableChain(current).orElse(current)
      locationFromMap(newCurrent, direction) match {
        case None =>
          val loc = loadLocation(room) match {
            case Nil =>
              val l = saveLocation(room).some
              transition(newCurrent, direction, l)
              l
            case xs =>
              val l = saveLocation(room).some
              transition(newCurrent, direction, l, isWeak = true)
              l
          }

          become(rec(loc))
        case Some(loc) =>
          become(rec(loc.some))
      }
    case PathTo(target) =>
      val path = pathTo(current, target)
      sender ! RawRead(path.map(_.id).mkString)
  }

  private def location(room: RoomSnapshot) = loadLocation(room) match {
    case Nil => saveLocation(room).some
    case loc :: Nil => loc.some
    case _ => none
  }

  private def locationFromMap(currentOption: Option[Location], direction: Direction): Option[Location] = {
    currentOption.flatMap(current => loadTransition(current, direction))
  }

  private def transition(prevOption: Option[Location], direction: Direction, newOption: Option[Location], isWeak: Boolean = false) {
    for {
      prev <- prevOption
      curr <- newOption
    } yield loadTransition(prev, direction, curr).getOrElse(saveTransition(prev, direction, curr, isWeak))
  }
}

case class PathTo(target: Location)
