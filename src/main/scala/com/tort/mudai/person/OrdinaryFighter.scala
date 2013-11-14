package com.tort.mudai.person

import akka.actor.{Props, Actor, ActorRef}
import com.tort.mudai.mapper.LocationPersister
import com.tort.mudai.event.{PeaceStatusEvent, FightRoundEvent}
import com.tort.mudai.command.RenderableCommand

class OrdinaryFighter(person: ActorRef, persister: LocationPersister, mapper: ActorRef) extends Actor {
  import context._

  val antiBasher = actorOf(Props(classOf[AntiBasher]))
  val attacker = actorOf(Props(classOf[OrdinaryAttacker], person))

  def receive = rec(false)

  /*
   * Define universal criteria for battle start and finish
   */
  def rec(peaceStatus: Boolean): Receive = {
    case e@Attack(target, number) =>
      person ! RequestPulses
      antiBasher ! e
      attacker ! e
    case e@FightRoundEvent(_, _, _) =>
      sender ! RequestPulses
      become(rec(peaceStatus = false))
    case e@PeaceStatusEvent() =>
      sender ! YieldPulses
      become(rec(peaceStatus = true))
    case RequestPulses => person ! RequestPulses
    case YieldPulses => person ! YieldPulses
    case c: RenderableCommand if sender == antiBasher => person ! c
    case c: RenderableCommand if sender == attacker => person ! c
    case e =>
      antiBasher ! e
      attacker ! e
  }
}
