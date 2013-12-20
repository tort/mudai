package com.tort.mudai

import akka.actor.{Actor, ActorRef, Props, ActorSystem}
import com.tort.mudai.person._
import com.tort.mudai.mapper._
import com.tort.mudai.event.GlanceEvent
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.Snoop
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event.GlanceEvent
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.Snoop
import com.tort.mudai.task.TravelToTerminated

object Runner {
  def main(args: Array[String]) {
    val system = ActorSystem()
    val persister = new SQLLocationPersister
    val pathHelper = new JGraphtPathHelper(persister)
    val mapper = system.actorOf(Props(classOf[MudMapper], pathHelper, persister, persister))
    val person = system.actorOf(Props(classOf[Person], args(0), args(1), mapper, pathHelper, persister))
    adHocSession(person, new MudConsole)
  }
}

object QuestRunner {
  def main(args: Array[String]) {
    val system = ActorSystem()
    val persister = new SQLLocationPersister
    val pathHelper = new JGraphtPathHelper(persister)
    val mapper = system.actorOf(Props(classOf[MudMapper], pathHelper, persister, persister))
    val person = system.actorOf(Props(classOf[Person], args(0), args(1), mapper, pathHelper, persister))
    val mudConsole = new MudConsole
    system.actorOf(Props(classOf[QuestScheduler], person, mudConsole, mapper, persister, pathHelper))
    mudConsole.userInputLoop(person, Map())
  }
}

class QuestScheduler(val person: ActorRef, val console: MudConsole, val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper) extends QuestHelper {

  import context._
  import scala.concurrent.duration._

  system.scheduler.schedule(1 second, 30 minutes, self, TimeForQuest)

  person ! Snoop(console.writer)

  def receive = {
    case TimeForQuest =>
      become(waitGlance)

      person ! Login
  }

  private def waitGlance: Receive = {
    case GlanceEvent(_, None) =>
      roam(Seq("Малиновый сад", "Болото", "Птичий лес", "Деревенский колодец", "Пустырь"))
  }

  def roam(zones: Seq[String]) = {
    zones match {
      case Nil =>
        person ! GoTo(persister.locationByTitle("Комната отдыха").head)
        become(waitTravelTerminated)
      case x :: xs =>
        person ! RoamZone(Zone.name(x))
        become(waitFinishRoaming(xs))
    }
  }

  def waitFinishRoaming(zones: Seq[String]): Receive = {
    case RoamingFinished =>
      roam(zones)
  }

  def waitTravelTerminated: Receive = {
    case TravelToTerminated(_, _) =>
      person ! new SimpleCommand("постой")
      person ! new SimpleCommand("0")
  }
}

case object TimeForQuest
