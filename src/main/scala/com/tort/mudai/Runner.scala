package com.tort.mudai

import akka.actor.{Actor, ActorRef, Props, ActorSystem}
import com.tort.mudai.person._
import com.tort.mudai.mapper.{MudMapper, PathHelper, SQLLocationPersister}
import com.tort.mudai.event.GlanceEvent
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.Snoop
import com.tort.mudai.command.SimpleCommand

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

  system.scheduler.schedule(1 second, 20 minutes, self, TimeForQuest)

  person ! Snoop(console.writer)

  def receive = {
    case TimeForQuest =>
      become {
        case GlanceEvent(room, direction) =>
          println("GLANCE")
          person ! StartQuest("бобры")
          become {
            case QuestFinished =>
              person ! StartQuest("белый паук")
              become {
                case QuestFinished =>
                  person ! new SimpleCommand("постой")
                  person ! new SimpleCommand("0")
                  unbecome()
                  unbecome()
                  unbecome()
              }
          }
      }
      person ! Login
  }
}

case object TimeForQuest
