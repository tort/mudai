package com.tort.mudai.mapper

import collection.mutable.{HashSet => MHashSet, HashMap => MHashMap, Map => MMap, Set => MSet}
import com.tort.mudai.RoomKey

class Location (
                     val title: String,
                     val desc: String,
                     val exits: Set[Directions],
                     var zone: Option[Zone] = None,
                     var directions: MMap[String, Location] = new MHashMap,
                     var waterSource: Option[String] = None,
                     var isShop: Boolean = false,
                     var isTavern: Boolean = false,
                     var knownMobs: MSet[Mob] = new MHashSet
                     ) extends RoomKey  {

  def addDirection(direction: String, location: Location) { directions.put(direction, location) }

  def markShop() {isShop = true}

  def markTavern() {isTavern = true}

  def getByDirection(direction: String) = directions.get(direction)
}

object Location {
  def apply(title: String, desc: String,  exits: Set[Directions]) = new Location(title, desc, exits)
}
