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


  def onArrived(travelTask: ActorRef, toDo: (Set[Location]) => Unit): Receive = {
    case TravelToTerminated(task, visited) if task == travelTask => toDo(visited)
    case command: SimpleCommand => person ! command
    case Pulse =>
      travelTask ! Pulse
    case e => travelTask ! e
  }

  def finishQuest(person: ActorRef) {
    person ! QuestFinished
    println("QUEST FINISHED")
  }
}

case class StartQuest(quest: String)

case object QuestFinished

case object DoneWithMob
