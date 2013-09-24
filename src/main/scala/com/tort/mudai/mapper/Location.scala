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

case class Exit(direction: String @@ Direction, isBorder: Boolean = false, closed: Boolean = false)

object Location {
  def apply(room: RoomSnapshot) = new Location("0", room.title, room.desc)

  def apply(id: String, title: String, desc: String) = new Location(id, title, desc)
}

class Transition(
                  val id: String,
                  val from: Location,
                  val direction: String @@ Direction,
                  val to: Location,
                  val isWeak: Boolean = false,
                  val isBorder: Boolean = false)

