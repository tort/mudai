package com.tort.mudai.person

import com.tort.mudai.mapper._
import akka.actor.ActorRef
import com.tort.mudai.event.GlanceEvent
import scalaz._
import Scalaz._
import Mob._

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

  //TODO remove param
  private def lookForMob(targets: Set[Mob], caller: ActorRef)(travel: ActorRef): Receive = {
    case e@GlanceEvent(roomSnapshot, _) =>
      val visibles = roomSnapshot.mobs.map(mobString => {
        tryRecognizeByFullName(mobString).orElse(tryRecognizeByShortname(mobString))
      }).flatten

      visibles.toSet.intersect(targets) match {
        case visibleTargets if visibleTargets.isEmpty => person ! NoTargetsFound
        case visibleTargets => person ! MobFound(visibleTargets, visibles)
      }

      travel ! e
  }


  private def tryRecognizeByFullName(mobString: String): Option[Mob] = {
    persister.mobByFullName(fullName(mobString))
  }

  private def tryRecognizeByShortname(mobString: String): Option[Mob] = {
    if (mobString.endsWith(" стоит здесь.") || mobString.endsWith(" сидит здесь.")) {
      val sn = shortName(mobString.dropRight(13))
      persister.allMobsByShortName(sn) match {
        case Nil => None
        case x :: Nil => Some(x)
        case x :: xs => None
      }
    } else None
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

case object NoTargetsFound
