package com.tort.mudai.person

import akka.actor._
import com.tort.mudai.event._
import com.tort.mudai.command.{WalkCommand, RenderableCommand, SimpleCommand}
import com.tort.mudai.mapper._
import com.tort.mudai.task.TravelTo
import scala.concurrent.duration._
import akka.actor.Terminated
import scalaz.@@

class Person(login: String, password: String, mapper: ActorRef, pathHelper: PathHelper, persister: LocationPersister) extends Actor {

  import context._

  val adapter = actorOf(Props[Adapter])
  val snoopable = actorOf(Props[Snoopable])
  val fighter = actorOf(Props(classOf[Fighter]))
  val roamer = actorOf(Props(classOf[Roamer], mapper, pathHelper, persister))
  val provisioner = actorOf(Props(classOf[Provisioner]))
  val statusTranslator = actorOf(Props(classOf[StatusTranslator]))
  val simpleQuest = actorOf(Props(classOf[SimpleQuest]))
  val coreTasks = Seq(mapper, fighter, statusTranslator, provisioner, roamer, simpleQuest)

  system.scheduler.schedule(0 millis, 500 millis, self, Pulse)

  def receive: Receive = rec(coreTasks, Nil)

  def rec(tasks: Seq[ActorRef], pulseSubscribers: Seq[ActorRef]): Receive = {
    case snoop: Snoop => snoopable ! snoop
    case rawRead: RawRead => snoopable ! rawRead
    case rawWrite: RawWrite => adapter ! rawWrite
    case Login =>
      become(rec(tasks :+ sender, pulseSubscribers))
      adapter ! Login
    case Zap => adapter ! Zap
    case e: LoginPromptEvent => adapter ! new SimpleCommand(login)
    case e: PasswordPromptEvent => adapter ! new SimpleCommand(password)
    case c: RenderableCommand => adapter ! c
    case e@GoTo(loc) =>
      val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister))
      become(rec(travelTask +: tasks, travelTask +: pulseSubscribers))
      watch(travelTask)
      travelTask ! e
    case StartQuest =>
      val quest = actorOf(Props(classOf[Quest], mapper, pathHelper, persister))
      become(rec(tasks :+ quest, pulseSubscribers))
      quest ! StartQuest
    case RequestPulses =>
      val newSubscribers: Seq[ActorRef] = tasks.filter(t => (sender +: pulseSubscribers).contains(t))
      become(rec(tasks, newSubscribers))
    case YieldPulses =>
      become(rec(tasks, pulseSubscribers.filterNot(_ == sender)))
    case Terminated(ref) =>
      become(rec(tasks.filterNot(_ == ref), pulseSubscribers.filterNot(_ == ref)))
      snoopable ! RawRead("### terminated " + ref.getClass.getName)
    case Pulse =>
      pulseSubscribers.headOption.map(_ ! Pulse)
    case e => tasks.filter(_ != sender).foreach(_ ! e)
  }
}

class StatusTranslator extends Actor {
  def receive = rec

  val maxStamina = 135.0
  def rec: Receive = {
    case StatusLineEvent(health, stamina, exp, level, gold) =>
      sender ! StaminaChange(stamina * 100 / maxStamina)
  }
}

class SimpleQuest extends Actor {
  def receive = {
    case ActivateTrigger("На сеновале", direction, "На чердаке") =>
      sender ! new SimpleCommand("приставить лестница")
      sender ! new WalkCommand(direction)
  }
}

case class ActivateTrigger(from: String, direction: String @@ Direction, to: String)

case class StaminaChange(stamina: Double)

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
