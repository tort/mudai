package com.tort.mudai.person

import com.tort.mudai.mapper._
import akka.actor.ActorRef
import com.tort.mudai.event.GlanceEvent
import scalaz._
import Scalaz._

class Searcher(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {
  def receive = {
    case FindMobs(mobs, area) =>
      findMobs(mobs, area, sender, () => onAllVisited(sender))
  }

  private def onAllVisited(caller: ActorRef) {
    caller ! SearchFinished
  }

  private def findMobs(mobs: Set[Mob], area: Set[Location], caller: ActorRef, onAllVisited: () => Unit) {
    area |> visitAll(mobs, caller, onAllVisited)
  }

  private def visitAll(mobs: Set[Mob], caller: ActorRef, onAllVisited: () => Unit)(toVisit: Set[Location]): Unit = {
    visit(lookForMob(mobs, caller), onAllVisited)(toVisit)
  }

  private def lookForMob(targets: Set[Mob], caller: ActorRef)(travel: ActorRef): Receive = {
    case e@GlanceEvent(roomSnapshot, _) =>
      if (roomSnapshot.mobs.toSet.intersect(targets.map(_.fullName)).size > 0) {
        val visibles = roomSnapshot.mobs.map(fullName => persister.mobByFullName(fullName)).flatten
          caller ! MobFound(visibles.toSet.intersect(targets), visibles)
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
}

case class FindMobs(mobs: Set[Mob], area: Set[Location])

case object SearchFinished

case class MobFound(targets: Set[Mob], visibles: Seq[Mob])
