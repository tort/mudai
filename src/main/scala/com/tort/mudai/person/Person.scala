package com.tort.mudai.person

import akka.actor._
import com.tort.mudai.event._
import com.tort.mudai.command.{KillCommand, RenderableCommand, SimpleCommand}
import com.tort.mudai.mapper._
import com.tort.mudai.task.TravelTo
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import com.tort.mudai.event.KillEvent
import com.tort.mudai.event.FightRoundEvent
import akka.actor.Terminated

class Person(login: String, password: String, mapper: ActorRef, pathHelper: PathHelper, persister: LocationPersister) extends Actor {

  import context._

  val adapter = actorOf(Props[Adapter])
  val snoopable = actorOf(Props[Snoopable])
  val fighter = actorOf(Props(classOf[Fighter]))
  val roamer = actorOf(Props(classOf[Roamer], mapper, pathHelper, persister))
  val coreTasks = Seq(fighter, mapper, roamer)

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
      val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper))
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

class Snoopable extends Actor {

  import context._

  def receive = {
    case Snoop(onRawRead) => become {
      case rawRead: RawRead => onRawRead(rawRead.text)
      case StopSnoop => unbecome()
    }
  }
}

class Fighter extends Actor {

  import context._

  def receive = {
    case FightRoundEvent(state, target, targetState) =>
      println("FIGHT STARTED")
      val person = sender
      person ! RequestPulses
      become {
        case KillEvent(target, exp) =>
          println("FIGHT FINISHED")
          person ! YieldPulses
          unbecome()
      }
  }
}

class Roamer(mapper: ActorRef, pathHelper: PathHelper, persister: LocationPersister) extends Actor {
  implicit val timeout = Timeout(5 seconds)

  import context._
  import persister._

  def receive = roam

  def roam: Receive = {
    case Roam =>
      val person = sender
      println("ROAMING STARTED")
      val future = for {
        f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
      } yield f

      future onSuccess {
        case current =>
          current.foreach(l => become(visit(person, killablesHabitation :+ l)))
      }
  }

  def visit(person: ActorRef, locations: Seq[Location]): Receive = locations match {
    case Nil =>
      println("ROAMING FINISHED")
      roam
    case x :: xs =>
      println("VISIT " + x.title)
      val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper))
      watch(travelTask)
      travelTask ! GoTo(x)

    {
      case Terminated(ref) if ref == travelTask =>
        become(visit(person, xs))
      case e@GlanceEvent(room, direction) =>
        room.mobs.flatMap(mobByFullName(_)).filter(_.killable).headOption.foreach {
          case mob =>
            mob.alias.foreach {
              case x =>
                println(new KillCommand(x).render)
                person ! new KillCommand(x)
            }
        }
        travelTask ! e
      case c: RenderableCommand => person ! c
      case e => travelTask ! e
    }
  }
}

case class Snoop(onRawRead: (String) => Unit)

case object StopSnoop

case object CurrentLocation

case class RawWrite(line: String)

case class GoTo(loc: Location)

case object Pulse

case object RequestPulses

case object YieldPulses

case object Roam
