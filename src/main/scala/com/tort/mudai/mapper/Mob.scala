package com.tort.mudai.mapper

import scalaz._
import Scalaz._
import com.tort.mudai.mapper.Mob._

class Mob(val id: String,
               val fullName: String @@ FullName,
               val shortName: Option[String @@ ShortName],
               val alias: Option[String @@ Alias],
               val killable: Boolean,
               val genitive: Option[String @@ Genitive],
               val isAssisting: Boolean,
               val canFlee: Boolean,
               val isAgressive: Boolean,
               val priority: Int,
               val isFragging: Boolean,
               val summoner: Boolean,
               val accusative: Option[String @@ Accusative],
               val globalTarget: Boolean) {

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
  trait Accusative
  trait FullName
  trait Alias

  def fullName(fn: String): String @@ FullName = Tag(fn)

  def shortName(shortName: String): String @@ ShortName = Tag(shortName)

  def genitive(genitive: String): String @@ Genitive = Tag(genitive)

  def accusative(accusative: String): String @@ Accusative = Tag(accusative)

  def alias(a: String): String @@ Alias = Tag(a)
}
