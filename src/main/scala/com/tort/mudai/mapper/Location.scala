package com.tort.mudai.mapper

import com.tort.mudai.RoomSnapshot
import scalaz._
import Scalaz._
import com.tort.mudai.mapper.Location.LocationId

class Location(
                val id: String @@ LocationId,
                val title: String,
                val desc: String,
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

  def locationId(id: String): String @@ LocationId = Tag(id)

  def apply(room: RoomSnapshot) = new Location(locationId("0"), room.title, room.desc)

  def apply(id: String, title: String, desc: String) = new Location(locationId(id), title, desc)

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

