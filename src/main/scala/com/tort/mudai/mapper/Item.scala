package com.tort.mudai.mapper

import scala.Option
import scalaz._
import Scalaz._
import Item._

class Item( val id: String,
            val fullName: String,
            val shortName: Option[String],
            val alias: Option[String],
            val objectType: Option[String])

object Item {
  implicit val itemEquality: Equal[Item] = Equal.equal(_.id === _.id)
  implicit val fullNameEquality: Equal[String @@ FullName] = Equal.equal(_ == _)

  trait FullName
  trait ShortName
  trait Accusative

  def fullName(fn: String): String @@ FullName = Tag(fn)
  def accusative(accusative: String): String @@ Accusative = Tag(accusative)
}
