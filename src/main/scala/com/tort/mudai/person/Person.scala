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

  def rec(tasks: Seq[ActorRef], pulseSubscribers: Seq[ActorRef]): Receive = {
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
      become(rec(travelTask +: tasks, travelTask +: pulseSubscribers))
      watch(travelTask)
      travelTask ! e
    case RequestPulses =>
      val newSubscribers: Seq[ActorRef] = tasks.filter(t => (sender +: pulseSubscribers).contains(t))
      become(rec(tasks, newSubscribers))
      newSubscribers.foreach(x => println(x.getClass.getName))
    case YieldPulses =>
      become(rec(tasks, pulseSubscribers.filterNot(_ == sender)))
    case Terminated(ref) =>
      become(rec(tasks.filterNot(_ == ref), pulseSubscribers.filterNot(_ == ref)))
      snoopable ! RawRead("### terminated " + ref.getClass.getName)
    case Pulse => pulseSubscribers.headOption.map(_ ! Pulse)
    case e => tasks.filter(_ != sender).foreach(_ ! e)
  }
}

class Provisioner() extends Actor {

  import context._

  def receive = rec

  def rec: Receive = {
    case Roam(zone) =>
      val person = sender
      sender ! new SimpleCommand("держ свеч")
      val feeder = system.scheduler.schedule(0 millis, 10 minutes, self, Feed)
      become(onRoaming(feeder, person))

  }

  def onRoaming(feeder: Cancellable, person: ActorRef): Receive = {
    case LightDimmedEvent() =>
      sender ! new SimpleCommand("снять свеч")
      sender ! new SimpleCommand("брос свеч")
      sender ! new SimpleCommand("держ свеч")
    case Feed =>
      become(onRoaming(feeder, person) orElse onPulse(feeder, person))
      println("FEED")
      person ! RequestPulses
    case RoamingFinished =>
      become(rec)
      feeder.cancel()
      sender ! new SimpleCommand("снять свеч")
  }

  def onPulse(feeder: Cancellable, person: ActorRef): Receive = {
    case Pulse =>
      become(onRoaming(feeder, person))
      sender ! new SimpleCommand("взять 4 хлеб меш")
      sender ! new SimpleCommand("есть хлеб")
      sender ! new SimpleCommand("есть хлеб")
      sender ! new SimpleCommand("есть хлеб")
      sender ! new SimpleCommand("есть хлеб")
      sender ! new SimpleCommand("есть хлеб")
      sender ! new SimpleCommand("есть хлеб")
      sender ! new SimpleCommand("полож все.хлеб меш")
      sender ! new SimpleCommand("пить мех")
      sender ! new SimpleCommand("пить мех")
      person ! YieldPulses
  }
}

case object Feed

case class NotHungryEvent() extends Event

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
