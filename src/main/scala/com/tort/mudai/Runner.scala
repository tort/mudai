package com.tort.mudai

import akka.actor.{Actor, ActorRef, Props, ActorSystem}
import com.tort.mudai.person.{StartQuest, Snoop, Login, Person}
import com.tort.mudai.mapper.{MudMapper, PathHelper, SQLLocationPersister}
import com.tort.mudai.event.GlanceEvent

object Runner {
  def main(args: Array[String]) {
    val system = ActorSystem()
    val persister = new SQLLocationPersister
    val pathHelper = new PathHelper(persister)
    val mapper = system.actorOf(Props(classOf[MudMapper], pathHelper, persister, persister))
    val person = system.actorOf(Props(classOf[Person], args(0), args(1), mapper, pathHelper, persister))
    adHocSession(person, new MudConsole)
  }
}

object QuestRunner {
  def main(args: Array[String]) {
    val system = ActorSystem()
    val persister = new SQLLocationPersister
    val pathHelper = new PathHelper(persister)
    val mapper = system.actorOf(Props(classOf[MudMapper], pathHelper, persister, persister))
    val person = system.actorOf(Props(classOf[Person], args(0), args(1), mapper, pathHelper, persister))
    val mudConsole = new MudConsole
    system.actorOf(Props(classOf[QuestScheduler], person, mudConsole))
    mudConsole.userInputLoop(person, Map())
  }
}

class QuestScheduler(person: ActorRef, console: MudConsole) extends Actor {
  import context._
  import scala.concurrent.duration._

  system.scheduler.schedule(1 second, 5 minutes, self, TimeForQuest)

  person ! Snoop(console.writer)

  def receive = {
    case TimeForQuest =>
      become {
        case GlanceEvent(room, direction) =>
          println("GLANCE")
          person ! StartQuest
          unbecome()
      }
      person ! Login
  }
}

case object TimeForQuest
