package com.tort.mudai.event

import com.tort.mudai.RoomSnapshot
import com.tort.mudai.mapper.Direction._
import com.tort.mudai.mapper.{Exit, Direction}
import scalaz._

class GlanceTrigger extends EventTrigger[GlanceEvent] {
  val lister = new DirectionLister()
  val MovePattern = ("(?ms).*Вы поплелись (?:на )?(" + lister.listDirections() + ")\\.\r?\n.*").r
  val GlancePattern = ("(?ms).*" +
    "\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s(.*)" +
    "\u001B\\[0\\;36m\\[ Exits: ([nsewudNSEWUD\\s\\(\\)]*) \\]\u001B\\[0\\;37m\r?\n" +
    "(?:\u001B\\[1\\;37mСнежный ковер лежит у вас под ногами.\u001B\\[0\\;37m\r?\n)?" +
    "(?:\u001B\\[1\\;30mВы просто увязаете в грязи.\u001B\\[0\\;37m\r?\n)?" +
    "(?:\u001B\\[1\\;34mУ вас под ногами [^\n]* лед.\u001B\\[0\\;37m\r?\n)?" +
    "\u001B\\[1\\;33m(?:(.*)\r?\n)?" +
    "\u001B\\[1\\;31m(?:(.*)\r?\n)?" +
    "[^\n]*>.*").r

  override def matches(text: String) = {
    text.matches(GlancePattern.toString())
  }

  private def extractDirection(text: String): Option[String @@ Direction] = {
    text match {
      case MovePattern(directionName) => Some(Direction(directionName))
      case _ => None
    }
  }

  override def fireEvent(text: String) = {
    val direction = extractDirection(text)
    val GlancePattern(locationTitle, locationDesc, availableExits, objectsGroup, mobsGroup) = text

    val exits = availableExits.split(' ').map {
      case alias if alias.startsWith("(") =>
        val a = alias.drop(1).dropRight(1)
        Exit(aliasToDirection(a.toString), isBorder = a.head.isUpper, closed = true)
      case alias =>
        Exit(aliasToDirection(alias.toString), isBorder = alias.head.isUpper)
    }.toSet
    val objects = Option(objectsGroup).map(x => x.filterNot(c => c == '\r')).map(_.split('\n')).getOrElse(Array[String]())
    val mobs = Option(mobsGroup.filterNot(c => c == '\r')).map(_.split("\n")).getOrElse(Array[String]()).dropRight(1)

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
