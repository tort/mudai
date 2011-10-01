package com.tort.mudai.mapper

import java.util.UUID
import com.tort.mudai.RoomSnapshot

trait MapperTestHelpers {
  def randomString: String = {
    UUID.randomUUID().toString
  }

  def randomParam(param: String): String = {
    param + randomString
  }

  def randomTitle: String = {
    randomParam("title")
  }

  def randomDesc: String = {
    randomParam("desc")
  }

  def emptyDirections: Set[Directions] = {
    Set[Directions]()
  }

  def randomLocation = Location(
    randomTitle,
    randomDesc,
    emptyDirections
  )

  def randomMob = Mob(
    randomParam("name"),
    randomParam("descName"),
    Set[Location]()
  )

  def randomRoomSnapshot = RoomSnapshot(randomTitle, randomDesc, emptyDirections)
  def indexedRoomSnapshot(index: Int, directions: Set[Directions]) = RoomSnapshot("title" + index,  "desc" + index, directions)
  def randomRoomSnapshot(directions: Set[Directions]) = RoomSnapshot(randomTitle, randomDesc, directions)
}
