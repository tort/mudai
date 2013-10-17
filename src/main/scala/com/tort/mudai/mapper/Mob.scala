package com.tort.mudai.mapper

import scalaz._
import Scalaz._

class Mob(val id: String,
               val fullName: String,
               val shortName: Option[String],
               val alias: Option[String],
               val killable: Boolean,
               val genitive: Option[String]) {

  override def equals(obj: scala.Any) = obj match {
    case mob: Mob => this.id === mob.id
    case _ => false
  }

  override def hashCode() = id.hashCode
}

object Mob {
  implicit val mobEquality: Equal[Mob] = Equal.equal(_.id === _.id)

  trait ShortName
  trait Genitive

  def shortName(shortName: String): String @@ ShortName = Tag(shortName)

  def genitive(genitive: String): String @@ Genitive = Tag(genitive)
}

class Item( val id: String,
            val fullName: String,
            val shortName: Option[String],
            val alias: Option[String],
            val objectType: Option[String])
