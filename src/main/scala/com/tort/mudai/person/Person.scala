package com.tort.mudai.person

import akka.actor._
import com.tort.mudai.event._
import com.tort.mudai.command.{RenderableCommand, SimpleCommand}
import com.tort.mudai.mapper._
import com.tort.mudai.task.TravelTo
import scala.concurrent.duration._
import com.tort.mudai.mapper.Zone.ZoneName
import scalaz._
import Scalaz._
import com.tort.mudai.quest._
import scala.Some
import com.tort.mudai.mapper.NameZone
import akka.actor.Terminated
import com.tort.mudai.mapper.MoveEvent
import com.tort.mudai.mapper.Mob.Alias

class Person(login: String, password: String, mapper: ActorRef, pathHelper: PathHelper, persister: LocationPersister) extends Actor {

  import context._

  val adapter = actorOf(Props[Adapter])
  val snoopable = actorOf(Props[Snoopable])
  val fighter = actorOf(Props(classOf[TraderFighter], self, persister, mapper))
  val roamer = actorOf(Props(classOf[Roamer], mapper, pathHelper, persister, self))
  val provisioner = actorOf(Props(classOf[Provisioner]))
  val statusTranslator = actorOf(Props(classOf[StatusTranslator]))
  val whiteSpiderQuest = actorOf(Props(classOf[WhiteSpiderQuest], mapper, pathHelper, persister, self))
  val simpleQuest = actorOf(Props(classOf[SimpleQuest], mapper, persister, pathHelper, self))
  val mainRoqueQuest = actorOf(Props(classOf[MainRogueQuest], mapper, persister, pathHelper, self))
  val oldHunterQuest = actorOf(Props(classOf[OldHunterQuest], mapper, persister, pathHelper, self))
  val forestKeeperQuest = actorOf(Props(classOf[ForestKeeperQuest], mapper, persister, pathHelper, self))
  val rogueCampQuest = actorOf(Props(classOf[RogueCampQuest], mapper, persister, pathHelper, self))
  val rogueForestQuest = actorOf(Props(classOf[RogueForestQuest], mapper, persister, pathHelper, self))
  val woodpeckersQuest = actorOf(Props(classOf[WoodpeckersQuest], mapper, persister, pathHelper, self))
  val awfulTaleQuest = actorOf(Props(classOf[AwfulTaleQuest], mapper, persister, pathHelper, self))
  val polovtsianCampQuest = actorOf(Props(classOf[PolovtsianCampQuest], mapper, persister, pathHelper, self))
  val antonQuest = actorOf(Props(classOf[AntonQuest], mapper, persister, pathHelper, self))
  val prospection = actorOf(Props(classOf[Prospection], mapper, persister, pathHelper, self))
  val alchemy = actorOf(Props(classOf[Alchemy], self, persister))
  val quests = Map[String, ActorRef](
    "белый паук" -> whiteSpiderQuest,
    "рысь" -> simpleQuest,
    "глава" -> mainRoqueQuest,
    "угодья" -> oldHunterQuest,
    "хозяин леса" -> forestKeeperQuest,
    "лагерь разбойников" -> rogueCampQuest,
    "инструмент кузнеца" -> rogueForestQuest,
    "дятлы" -> woodpeckersQuest,
    "страшная сказка" -> awfulTaleQuest,
    "половцы" -> polovtsianCampQuest,
    "антон" -> antonQuest,
    "копка" -> prospection)
  val passages = actorOf(Props(classOf[Passages], persister, self))
  val coreTasks = Seq(mapper, fighter, statusTranslator, provisioner, roamer, passages, alchemy) ++ quests.values

  system.scheduler.schedule(0 millis, 500 millis, self, Pulse)

  def receive: Receive = rec(coreTasks, Nil)

  def rec(tasks: Seq[ActorRef], pulseSubscribers: Seq[ActorRef]): Receive = {
    case snoop: Snoop => snoopable ! snoop
    case rawRead: RawRead =>
      snoopable ! rawRead
      quests.foreach(_._2 ! rawRead)
      passages ! rawRead
    case rawWrite: RawWrite => adapter ! rawWrite
    case Login =>
      become(rec(tasks :+ sender, pulseSubscribers))
      adapter ! Login
    case Zap => adapter ! Zap
    case e: LoginPromptEvent => adapter ! new SimpleCommand(login)
    case e: PasswordPromptEvent => adapter ! new SimpleCommand(password)
    case c@FleeCommand(_) =>
      mapper ! c
      adapter ! c
    case c: RenderableCommand =>
      adapter ! c
    case e@GoTo(loc) =>
      val travelTask = actorOf(Props(classOf[TravelTo], pathHelper, mapper, persister, self))
      become(rec(travelTask +: tasks, travelTask +: pulseSubscribers))
      watch(travelTask)
      travelTask ! e
    case Quests =>
      sender ! quests.keySet
    case StartQuest(quest) =>
      quests(quest) ! StartQuest
      provisioner ! StartQuest(quest)
    case e@MoveEvent(Some(from), direction, to) if from.zone.map(_.id) /== to.zone.map(_.id) =>
      from.zone.foreach {
        case z =>
          mapper ! NameZone(z.name, from.some)
      }
      tasks.filter(_ != sender).foreach(_ ! e)
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

case object Feed

case class NotHungryEvent() extends Event

case object ReadyForFight

case class Snoop(onRawRead: (String) => Unit)

case object StopSnoop

case object CurrentLocation

case class RawWrite(line: String)

case class GoTo(loc: Location)

case object Pulse

case object RequestPulses

case object Quests

case object YieldPulses

case class RoamZone(zoneName: String @@ ZoneName)

case class RoamMobsInArea(targets: Set[Mob], area: Set[Location])

case class Attack(target: Mob, number: Option[Int] = None)
case class AttackByAlias(target: String @@ Alias)
