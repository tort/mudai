package com.tort.mudai.mapper

import com.tort.mudai.RoomKey

class Location (
                     val title: String,
                     val desc: String,
                     val exits: Set[Exit],
                     var zone: Option[Zone] = None,
                     var waterSource: Option[String] = None,
                     var isShop: Boolean = false,
                     var isTavern: Boolean = false
                     ) extends RoomKey  {

  def markShop() {isShop = true}

  def markTavern() {isTavern = true}
}

case class Exit(direction: Direction, border: Boolean = false)

object Location {
  def apply(title: String, desc: String,  exits: Set[Exit]) = new Location(title, desc, exits)
}
