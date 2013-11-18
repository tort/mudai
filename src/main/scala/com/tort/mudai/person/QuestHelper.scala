package com.tort.mudai.person

import scala.Unit
import akka.actor.{Props, Actor, ActorRef}
import com.tort.mudai.mapper._
import com.tort.mudai.task.TravelTo
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.task.TravelToTerminated

trait QuestHelper extends Actor {
  def pathHelper: PathHelper

  def mapper: ActorRef

  def persister: LocationPersister

  def person: ActorRef

  def goAndDo(targetLocation: Location, person: ActorRef, toDo: (Set[Location]) => Unit = (x) => {}, pfProcess: (ActorRef) => Receive = (x) => PartialFunction.empty) {
    val travelTask = context.actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister, person))
    travelTask ! GoTo(targetLocation)

    context.become(pfProcess(travelTask).orElse(onArrived(travelTask, toDo)))
  }

  def search(mobs: Set[Mob], area: Set[Location])(onEvent: (ActorRef, Boolean) => Receive) = {
    val searcher = context.actorOf(Props(classOf[Searcher], mapper, persister, pathHelper, person))
    searcher ! FindMobs(mobs, area)
    context.become(onEvent(searcher, false).orElse(passEvents(searcher)))
  }

  private def passEvents(searcher: ActorRef): Receive = {
    case e => searcher ! e
  }

  private def onArrived(travelTask: ActorRef, toDo: (Set[Location]) => Unit): Receive = {
    case TravelToTerminated(task, visited) if task == travelTask => toDo(visited)
    case command: SimpleCommand => person ! command
    case Pulse =>
      travelTask ! Pulse
    case e => travelTask ! e
  }

  def finishQuest(person: ActorRef) {
    person ! QuestFinished
    person ! YieldPulses
    println("### QUEST FINISHED")
  }
}

case class StartQuest(quest: String)

case object QuestFinished
