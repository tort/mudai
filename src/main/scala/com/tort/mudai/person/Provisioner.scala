package com.tort.mudai.person

import akka.actor.{Cancellable, Props, ActorRef, Actor}
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event.LightDimmedEvent
import scala.concurrent.duration._

class Provisioner extends Actor {

  import context._

  val feeder = actorOf(Props(classOf[Feeder]))
  val staminaControl = actorOf(Props(classOf[StaminaControl]))
  val tasks = feeder :: staminaControl :: Nil

  def receive = rec

  def rec: Receive = {
    case StartQuest(quest) =>
      val person = sender
      val feedPulseEmitter = system.scheduler.schedule(0 millis, 10 minutes, feeder, Feed)

      become(onRoam(person, feedPulseEmitter, Nil))
    case Roam(zone) =>
      val person = sender
      sender ! new SimpleCommand("держ свеч")
      val feedPulseEmitter = system.scheduler.schedule(0 millis, 10 minutes, feeder, Feed)

      become(onRoam(person, feedPulseEmitter, Nil))
  }

  def onRoam(person: ActorRef, feedPulseEmitter: Cancellable, pulseSubscribers: Seq[ActorRef]): Receive = {
    case LightDimmedEvent() =>
      sender ! new SimpleCommand("снять свеч")
      sender ! new SimpleCommand("брос свеч")
      sender ! new SimpleCommand("держ свеч")
    case QuestFinished =>
      become(rec)
      feedPulseEmitter.cancel()
      sender ! new SimpleCommand("снять свеч")
    case RoamingFinished =>
      become(rec)
      feedPulseEmitter.cancel()
      sender ! new SimpleCommand("снять свеч")
    case RequestPulses =>
      become(onRoam(person, feedPulseEmitter, pulseSubscribers :+ sender))
      if (pulseSubscribers.isEmpty)
        person ! RequestPulses
    case YieldPulses =>
      val newSubscribers: Seq[ActorRef] = pulseSubscribers.filterNot(_ == sender)
      become(onRoam(person, feedPulseEmitter, newSubscribers))
      if (newSubscribers.isEmpty) {
        person ! YieldPulses
      }
    case c: SimpleCommand => person ! c
    case Pulse =>
      pulseSubscribers.headOption.map(_ ! Pulse)
    case e => tasks.filter(_ != sender).foreach(_ ! e)
  }
}

class StaminaControl extends Actor {

  import context._

  def receive = {
    case StaminaChange(stamina) =>
      if (stamina < 30) {
        sender ! RequestPulses
        become {
          case Pulse =>
            sender ! new SimpleCommand("отд")
            become {
              case StaminaChange(stamina) =>
                if (stamina > 70) {
                  sender ! new SimpleCommand("вст")
                  unbecome()
                  unbecome()
                  sender ! YieldPulses
                }
            }
        }
      }
  }
}

class Feeder extends Actor {

  import context._

  def receive = {
    case Feed =>
      sender ! RequestPulses
      become {
        case Pulse =>
          sender ! new SimpleCommand("взять 6 хлеб меш")
          sender ! new SimpleCommand("есть хлеб")
          sender ! new SimpleCommand("есть хлеб")
          sender ! new SimpleCommand("есть хлеб")
          sender ! new SimpleCommand("есть хлеб")
          sender ! new SimpleCommand("есть хлеб")
          sender ! new SimpleCommand("есть хлеб")
          sender ! new SimpleCommand("полож все.хлеб меш")
          sender ! new SimpleCommand("пить мех")
          sender ! new SimpleCommand("пить мех")
          unbecome()
          sender ! YieldPulses
      }
  }
}
