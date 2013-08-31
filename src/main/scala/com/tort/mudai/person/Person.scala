package com.tort.mudai.person

import akka.actor._
import com.tort.mudai.event._
import com.tort.mudai.command.{KillCommand, RenderableCommand, SimpleCommand}
import com.tort.mudai.mapper._
import com.tort.mudai.task.TravelTo
import scala.concurrent.duration._
import akka.actor.Terminated

class Person(login: String, password: String, mapper: ActorRef, pathHelper: PathHelper, persister: LocationPersister) extends Actor {

  import context._

  val adapter = actorOf(Props[Adapter])
  val snoopable = actorOf(Props[Snoopable])
  val fighter = actorOf(Props(classOf[Fighter]))
  val roamer = actorOf(Props(classOf[Roamer], mapper, pathHelper, persister))
  val provisioner = actorOf(Props(classOf[Provisioner]))
  val coreTasks = Seq(fighter, mapper, provisioner, roamer)

  system.scheduler.schedule(0 millis, 500 millis, self, Pulse)

  def receive: Receive = rec(coreTasks, roamer :: Nil)

  def rec(tasks: Seq[ActorRef], pulseRequests: Seq[ActorRef]): Receive = {
    case snoop: Snoop => snoopable ! snoop
    case rawRead: RawRead => snoopable ! rawRead
    case rawWrite: RawWrite => adapter ! rawWrite
    case Login => adapter ! Login
    case Zap => adapter ! Zap
    case e: LoginPromptEvent => adapter ! new SimpleCommand(login)
    case e: PasswordPromptEvent => adapter ! new SimpleCommand(password)
    case c: RenderableCommand => adapter ! c
    case e@GoTo(loc) =>
      val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister))
      become(rec(travelTask +: tasks, travelTask +: pulseRequests))
      watch(travelTask)
      travelTask ! e
    case RequestPulses =>
      become(rec(tasks, sender +: pulseRequests))
    case YieldPulses =>
      become(rec(tasks, pulseRequests.filterNot(_ == sender)))
    case Terminated(ref) =>
      become(rec(tasks.filterNot(_ == ref), pulseRequests.filterNot(_ == ref)))
      snoopable ! RawRead("### terminated " + ref.getClass.getName)
    case Pulse => pulseRequests.headOption.map(_ ! Pulse)
    case e => tasks.filter(_ != sender).foreach(_ ! e)
  }
}

class Provisioner() extends Actor {
  def receive = {
    case LightDimmedEvent() =>
      sender ! new SimpleCommand("снять свеч")
      sender ! new SimpleCommand("брос свеч")
      sender ! new SimpleCommand("держ свеч")
    case Roam(zone) =>
      sender ! new SimpleCommand("держ свеч")
    case RoamingFinished =>
      sender ! new SimpleCommand("снять свеч")
  }
}

case object NeedMem

case object ReadyForFight

case class Snoop(onRawRead: (String) => Unit)

case object StopSnoop

case object CurrentLocation

case class RawWrite(line: String)

case class GoTo(loc: Location)

case object Pulse

case object RequestPulses

case object YieldPulses

case class Roam(zoneName: String)

case class Attack(target: String)
