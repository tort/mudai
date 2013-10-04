package com.tort.mudai.person

import com.tort.mudai.mapper._
import akka.actor.ActorRef
import com.tort.mudai.event.GlanceEvent
import scalaz._
import Scalaz._

class Searcher(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {
  def receive = {
    case FindMobs(mobNames) =>
      val mobs = mobNames.map(m => persister.mobByFullName(m).get)

      findMobs(mobs, sender, () => onAllVisited(sender))
  }

  def onAllVisited(caller: ActorRef) {
    caller ! SearchFinished
  }

  def findMobs(mobs: Set[Mob], caller: ActorRef, onAllVisited: () => Unit) {
    whereMobLives(mobs) |> visitAll(mobs, caller, onAllVisited)
  }

  def visitAll(mobs: Set[Mob], caller: ActorRef, onAllVisited: () => Unit)(toVisit: Set[Location]): Unit = {
    visit(lookForMob(mobs, caller), onAllVisited)(toVisit)
  }

  private def lookForMob(mobs: Set[Mob], caller: ActorRef)(travel: ActorRef): Receive = {
    case e@GlanceEvent(roomSnapshot, _) =>
      val needed: Seq[String] = roomSnapshot.mobs.toSet.intersect(mobs.map(_.fullName)).toList
      needed match {
        case x :: xs =>
          caller ! MobFound(x)
        case Nil =>
      }
      travel ! e
  }

  def visit(pfProcess: (ActorRef) => Receive, onFinish: () => Unit)(toVisit: Set[Location]) {
    toVisit.toList match {
      case Nil =>
        onFinish()
      case x :: xs =>
        goAndDo(x, person, pfProcess = pfProcess, toDo = (visited) => {
          visit(pfProcess, onFinish)(xs.toSet -- visited)
        })
    }
  }

  //TODO fix get
  private def whereMobLives(mobs: Set[Mob]): Set[Location] = persister.loadLocations(mobZone(mobs.head).get)

  private def mobZone(mob: Mob): Option[Zone] = persister.locationByMob(mob.fullName).head.zone
}

case class FindMobs(mobs: Set[String])

case object SearchFinished

case class MobFound(fullName: String)
