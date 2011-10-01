package com.tort.mudai.event

import com.tort.mudai.mapper.Directions;

import com.google.inject.Inject
import com.tort.mudai.{Handler, RoomSnapshot}
import com.tort.mudai.task.{AbstractTask, EventDistributor}

class GlanceTrigger @Inject()(eventDistributor: EventDistributor) extends EventTrigger[GlanceEvent] {
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
    "\u001B\\[0\\;37m\r?\n" +
    "[^\n]*> ЪЫ$").r

  override def matches(text: String) = {
    text.matches(GlancePattern.toString())
  }

  private def extractDirection(text: String) = {
    text match {
      case MovePattern(direction) => Some(direction)
      case _ => None
    }
  }

  override def fireEvent(text: String) = {
    val direction = extractDirection(text)
    val GlancePattern(locationTitle, locationDesc, availableExits, objectsGroup, mobsGroup) = text

    val exits = Directions.values().filter(exit => availableExits.contains(exit.getAlias)).toSet
    val objects = Option(objectsGroup).map(_.split("\n")).getOrElse(Array[String]())
    val mobs = Option(mobsGroup).map(_.split("\n")).getOrElse(Array[String]())

    val roomSnapshot = new RoomSnapshot(
      locationTitle,
      locationDesc,
      exits,
      objects,
      mobs
    )

    eventDistributor.invoke(new Handler[GlanceEvent]() {
      override def handle(task: AbstractTask) {
        if (direction.isDefined) {
          task.glance(direction.get, roomSnapshot)
        } else {
          task.glance(roomSnapshot)
        }
      }
    })

    GlanceEvent(roomSnapshot, direction)
  }
}
