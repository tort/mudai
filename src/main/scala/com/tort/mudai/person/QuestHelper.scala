package com.tort.mudai.person

import scala.Unit
import akka.actor.{Terminated, Props, Actor, ActorRef}
import com.tort.mudai.mapper.{Location, LocationPersister, PathHelper}
import com.tort.mudai.task.TravelTo
import com.tort.mudai.command.SimpleCommand

trait QuestHelper extends Actor {
  def pathHelper: PathHelper

  def mapper: ActorRef

  def persister: LocationPersister

  def goAndDo(targetLocation: Location, person: ActorRef, toDo: () => Unit) {
    val travelTask = context.actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister, person))
    context.watch(travelTask)
    travelTask ! GoTo(targetLocation)

    context.become(onArrived(person, travelTask, toDo))
  }

  def onArrived(person: ActorRef, travelTask: ActorRef, toDo: () => Unit): Receive = {
    case Terminated(task) if (task == travelTask) => toDo()
    case command: SimpleCommand => person ! command
    case e => travelTask forward e
  }

  def finishQuest(person: ActorRef) {
    person ! QuestFinished
    println("QUEST FINISHED")
  }
}

case class StartQuest(quest: String)

case object QuestFinished
