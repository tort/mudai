package com.tort.mudai.mapper

import scalaz._
import Scalaz._
import com.tort.mudai.mapper.Mob.{Alias, ShortName}

class Mob(val id: String,
               val fullName: String,
               val shortName: Option[String @@ ShortName],
               val alias: Option[String @@ Alias],
               val killable: Boolean,
               val genitive: Option[String],
               val isAssisting: Boolean,
               val canFlee: Boolean,
               val isAgressive: Boolean,
               val priority: Int,
               val isFragging: Boolean) {

  override def equals(obj: scala.Any) = obj match {
    case mob: Mob => this.id === mob.id
    case _ => false
  }

  override def hashCode() = id.hashCode
}

object Mob {
  implicit val mobEquality: Equal[Mob] = Equal.equal(_.id === _.id)
  implicit val shortNameEquality: Equal[String @@ ShortName] = Equal.equal(_ == _)

  trait ShortName
  trait Genitive
  trait FullName
  trait Alias

  def fullName(fn: String): String @@ FullName = Tag(fn)

  def shortName(shortName: String): String @@ ShortName = Tag(shortName)

  def genitive(genitive: String): String @@ Genitive = Tag(genitive)

  def alias(a: String): String @@ Alias = Tag(a)
}

class Item( val id: String,
            val fullName: String,
            val shortName: Option[String],
            val alias: Option[String],
            val objectType: Option[String])
