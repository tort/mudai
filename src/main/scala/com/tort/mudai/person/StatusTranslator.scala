package com.tort.mudai.person

import akka.actor.Actor
import scalaz._
import Scalaz._
import com.tort.mudai.event.StatusLineEvent

class StatusTranslator extends Actor {
  import StatusTranslator._

  def receive = rec(0, 0, 0, 0)

  private def rec(currentHealth: Int,
          maxHealth: Int,
          currentStamina: Int,
          maxStamina: Int): Receive = {
    case StatusLineEvent(health, stamina, exp, mem, level, gold) =>
      val newStamina: Int = stamina
      val newHealth: Int = health

      val maxS: Int = Seq(newStamina, maxStamina).max
      val maxH: Int = Seq(newHealth, maxHealth).max

      if (currentStamina != newStamina) sender ! StaminaChange(newStamina * 100 / maxS)
      if (currentHealth != newHealth) sender ! HealthChange(newHealth * 100 / maxH)

      context.become(rec(newHealth, maxH, newStamina, maxS))
    case HealthRequest =>
      sender ! (currentHealth * 100 / maxHealth)
  }

  private def max[A](left: Int @@ A, right: Int @@ A): Int @@ A = if (left > right) left else right
}

object StatusTranslator {
  case class StaminaChange(stamina: Double)
  case class HealthChange(health: Int)
  case object HealthRequest

  sealed trait Health
  sealed trait Stamina

  def tagHealth(health: Int): Int @@ Health = Tag(health)
  def tagStamina(stamina: Int): Int @@ Stamina = Tag(stamina)

  val MaxHealth = 100

//  implicit val healthEquals: Equal[Int @@ Health] = Equal.equal[Int @@ Health]((l, r) => l == r)
//  implicit val staminaEquals: Equal[Int @@ Stamina] = Equal.equal(_ == _)
}
