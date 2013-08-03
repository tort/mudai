package com.tort.mudai.mapper

import com.tort.mudai.RoomSnapshot
import scalaz._
import Scalaz._

class Location(
                val id: String,
                val title: String,
                val desc: String,
                var zone: Option[Zone] = None,
                var waterSource: Option[String] = None,
                var isShop: Boolean = false,
                var isTavern: Boolean = false) {

  def markShop() {
    isShop = true
  }

  def markTavern() {
    isTavern = true
  }
}

case class Exit(direction: Direction, border: Boolean = false)

object Location {
  def apply(room: RoomSnapshot) = new Location("0", room.title, room.desc)

  def apply(id: String, title: String, desc: String) = new Location(id, title, desc)
}

class Transition(
                  val id: String,
                  val from: Location,
                  val direction: Direction,
                  val to: Location) {
  override def hashCode() = 41 + id.hashCode

  override def equals(obj: Any) = obj match {
    case null => false
    case t: Transition => id === t.id
    case _ => false
  }
}
