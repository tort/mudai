package com.tort.mudai.person

import akka.actor.Actor
import scalaz._
import Scalaz._
import com.tort.mudai.event.StatusLineEvent
import scalaz.Ordering.{EQ, LT, GT}

class StatusTranslator extends Actor {
  import StatusTranslator._

  def receive = rec(tagHealth(0), tagHealth(0), tagStamina(0), tagStamina(0))

  def rec(currentHealth: Int @@ Health,
          maxHealth: Int @@ Health,
          currentStamina: Int @@ Stamina,
          maxStamina: Int @@ Stamina): Receive = {
    case StatusLineEvent(health, stamina, exp, mem, level, gold) =>
      val newStamina = tagStamina(stamina)
      val newHealth = tagHealth(health)

      val maxS = max(newStamina, maxStamina)
      val maxH = max(newHealth, maxHealth)

      context.become(rec(newHealth, maxH, newStamina, maxS))

      if (currentStamina /== newStamina) sender ! StaminaChange(newStamina * 100 / maxS)
      if (currentHealth /== newHealth) sender ! HealthChange(newHealth * 100 / maxH)
    case HealthRequest =>
      sender ! (currentHealth * 100 / maxHealth)
  }

  def max[A](left: Int @@ A, right: Int @@ A): Int @@ A = if (left > right) left else right
}

object StatusTranslator {
  case class StaminaChange(stamina: Double)
  case class HealthChange(health: Int)
  case object HealthRequest

  trait Health
  trait Stamina

  def tagHealth(health: Int): Int @@ Health = Tag(health)
  def tagStamina(stamina: Int): Int @@ Stamina = Tag(stamina)

  val MaxHealth = 100

  implicit val healthEquals: Equal[Int @@ Health] = Equal.equal((left, right) => left == right)
  implicit val staminaEquals: Equal[Int @@ Stamina] = Equal.equal((left, right) => left == right)
}
