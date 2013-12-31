package com.tort.mudai.person

import com.tort.mudai.mapper._
import akka.actor.ActorRef
import scalaz._
import Scalaz._
import Mob._

class MobSearcher(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends Searcher {
  def receive = {
    case FindMobs(mobs, area) =>
      findMobs(mobs, area, sender, () => onAllVisited(sender))
  }

  protected override def onAllVisited(caller: ActorRef) {
    caller ! SearchFinished
  }

  protected def findMobs(mobs: Set[Mob], area: Set[Location], caller: ActorRef, onAllVisited: () => Unit) {
    area |> visitAll(onAllVisited, lookForMob(mobs, caller))
  }

  //TODO remove param
  private def lookForMob(targets: Set[Mob], caller: ActorRef)(travel: ActorRef): Receive = {
    case MobViewEvent(mobs) =>
      mobs.toSet.intersect(targets) match {
        case visibleTargets if visibleTargets.isEmpty => person ! NoTargetsFound
        case visibleTargets => person ! MobFound(visibleTargets, mobs)
      }
  }
}

case class FindMobs(mobs: Set[Mob], area: Set[Location])

case object SearchFinished

case class MobFound(targets: Set[Mob], visibles: Seq[Mob])

case object NoTargetsFound

