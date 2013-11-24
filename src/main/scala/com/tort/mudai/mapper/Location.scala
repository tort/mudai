package com.tort.mudai.mapper

import com.tort.mudai.RoomSnapshot
import scalaz._
import Scalaz._
import com.tort.mudai.mapper.Location.{Title, LocationId}

class Location(
                val id: String @@ LocationId,
                val title: String @@ Title,
                var zone: Option[Zone] = None,
                var waterSource: Option[String] = None,
                var isShop: Boolean = false,
                var isTavern: Boolean = false) {

  def markShop() {
    isShop = true
  }

  override def equals(obj: Any) = obj match {
    case location: Location => this === location
    case _ => false
  }
  override def hashCode() = id.hashCode

  def markTavern() {
    isTavern = true
  }
}

case class Exit(direction: String @@ Direction, isBorder: Boolean = false, closed: Boolean = false)

object Location {
  trait LocationId

  trait Title

  trait Desc

  def locationId(id: String): String @@ LocationId = Tag(id)

  def title(t: String): String @@ Title = Tag(t)

  def desc(d: String): String @@ Desc = Tag(d)

  def apply(room: RoomSnapshot) = new Location(locationId("0"), title(room.title), room.desc)

  def apply(id: String, t: String, desc: String) = new Location(locationId(id), title(t), desc)

  implicit val locationsEqual: Equal[Location] = Equal.equal(_.id === _.id)
  implicit val locationIdEqual: Equal[String @@ LocationId] = Equal.equal(_ == _)
}

class Transition(
                  val id: String,
                  val from: Location,
                  val direction: String @@ Direction,
                  val to: Location,
                  val isBorder: Boolean = false,
                  val isTriggered: Boolean = false)

