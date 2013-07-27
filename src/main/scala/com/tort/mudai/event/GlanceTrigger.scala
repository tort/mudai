package com.tort.mudai.event

import com.tort.mudai.RoomSnapshot
import com.tort.mudai.Metadata.Direction._
import com.tort.mudai.mapper.{Exit, Direction}

class GlanceTrigger extends EventTrigger[GlanceEvent] {
  val lister = new DirectionLister()
  val MovePattern = ("(?ms).*^Вы поплелись (?:на )?(" + lister.listDirections() + ")\\.\r?\n.*").r
  val GlancePattern = ("(?ms).*" +
    "\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s(.*)\r?\n\r?\n" +
    "\u001B\\[0\\;36m\\[ Exits: ([nsewudNSEWUD\\s\\(\\)]*) \\]\u001B\\[0\\;37m\r?\n" +
    "(?:\u001B\\[1\\;37mСнежный ковер лежит у Вас под ногами.\u001B\\[0\\;37m\r?\n)?" +
    "(?:\u001B\\[1\\;30mВы просто увязаете в грязи.\u001B\\[0\\;37m\r?\n)?" +
    "(?:\u001B\\[1\\;34mУ Вас под ногами толстый лед.\u001B\\[0\\;37m\r?\n)?" +
    "\u001B\\[1\\;33m(?:(.*)\r?\n)?" +
    "\u001B\\[1\\;31m(?:(.*)\r?\n)?" +
    "[^\n]*>.*").r

  override def matches(text: String) = {
    text.matches(GlancePattern.toString())
  }

  private def extractDirection(text: String): Option[Direction] = {
    text match {
      case MovePattern(directionName) => Some(nameToDirection(directionName))
      case _ => None
    }
  }

  override def fireEvent(text: String) = {
    val direction = extractDirection(text)
    val GlancePattern(locationTitle, locationDesc, availableExits, objectsGroup, mobsGroup) = text

    val split = availableExits.split(' ')
    val exits = split.map(alias => Exit(aliasToDirection(alias.toString), alias.head.isUpper)).toSet
    val objects = Option(objectsGroup).map(_.split("\n")).getOrElse(Array[String]())
    val mobs = Option(mobsGroup).map(_.split("\n")).getOrElse(Array[String]())

    val roomSnapshot = new RoomSnapshot(
      locationTitle,
      locationDesc,
      exits,
      objects,
      mobs
    )

    GlanceEvent(roomSnapshot, direction)
  }
}
