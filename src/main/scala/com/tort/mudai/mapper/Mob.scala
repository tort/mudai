package com.tort.mudai.mapper

import scalaz._
import Scalaz._

class Mob(val id: String,
               val fullName: String,
               val shortName: Option[String],
               val alias: Option[String],
               val killable: Boolean) {

  override def equals(obj: scala.Any) = obj match {
    case mob: Mob => this.id === mob.id
    case _ => false
  }

  override def hashCode() = id.hashCode
}

object Mob {
  implicit val mobEquality: Equal[Mob] = Equal.equal(_.id === _.id)
}

class Item( val id: String,
            val fullName: String,
            val shortName: Option[String],
            val alias: Option[String],
            val objectType: Option[String])
