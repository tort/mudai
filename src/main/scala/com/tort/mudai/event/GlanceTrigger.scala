package com.tort.mudai.event

import com.tort.mudai.{ItemAndNumber, RoomSnapshot}
import com.tort.mudai.mapper.{Item, Mob, Exit, Direction}
import Mob._
import com.tort.mudai.mapper.Direction._
import scalaz._
import Scalaz._

class GlanceTrigger extends EventTrigger[GlanceEvent] {
  val lister = new DirectionLister()
  val MovePattern = ("(?ms).*Вы поплелись (?:на )?(" + lister.listDirections() + ")\\.\r?\n.*").r
  val GlancePattern = ("(?ms).*" +
    "\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m\r?\n[^а-яА-Я]{0,6}(.*)" +
    "\u001B\\[0\\;36m\\[ Exits: ([nsewudNSEWUD\\s\\(\\)]*) \\]\u001B\\[0\\;37m\r?\n" +
    "(?:\u001B\\[0\\;33mЗдесь выкопана ямка глубиной примерно в \\d{1,2} аршин(?:a|ов)?.\u001B\\[0\\;37m\r?\n)?" +
    "(?:\u001B\\[\\d\\;37mСнежный ковер лежит у вас под ногами.\u001B\\[0\\;37m\r?\n)?" +
    "(?:\u001B\\[1\\;30mВы просто увязаете в грязи.\u001B\\[0\\;37m\r?\n)?" +
    "(?:\u001B\\[1\\;34mУ вас под ногами [^\n]* лед.\u001B\\[0\\;37m\r?\n)?" +
    "\u001B\\[1\\;33m(?:(.*)\r?\n)?" +
    "\u001B\\[1\\;31m(?:(.*)\r?\n)?" +
    "[^\n]*>.*").r

  override def matches(text: String) = {
    GlancePattern.findFirstIn(text).isDefined
  }

  private def extractDirection(text: String): Option[String @@ Direction] = {
    text match {
      case MovePattern(directionName) => Some(Direction(directionName))
      case _ => None
    }
  }

  private val MultiplePattern = """(.*) \[(\d+)\]""".r

  private val YellowAura = " ..желтая аура!"
  private val AirAura = " \n ..воздушная аура!"

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
    val objects =
      Option(objectsGroup)
        .map(x => x.filterNot(c => c == '\r'))
        .map(_.split('\n'))
        .getOrElse(Array[String]())
        .collect {
        case item if (item.endsWith(YellowAura)) => item.dropRight(YellowAura.length)
        case x => x
      }.map(x => x match {
        case MultiplePattern(obj, number) => ItemAndNumber(Item.fullName(obj), number.toInt)
        case s => ItemAndNumber(Item.fullName(s), 1)
      })
    val mobs = filterMobGroup(mobsGroup).getOrElse(Nil)

    val roomSnapshot = new RoomSnapshot(
      locationTitle,
      locationDesc,
      exits,
      objects,
      mobs.map(fullName(_))
    )

    GlanceEvent(roomSnapshot, direction)
  }

  private def filterMobGroup(mobsGroup: String): Option[Seq[String]] = {
    Option(mobsGroup.filterNot(c => c == '\r')).map(_.split( """\u001B\[0;31m""")(0).split("\n").toSeq |> filterFakes)
  }

  val StandupPattern = "(.*) вскочил[аои]? на ноги.".r
  val FightPattern = "(.*) (?:сокрушил|резанул|ударил|подстрелил|ободрал|оцарапал|рубанул|укусил)[аои]? вас.".r
  val PetMovePattern = "(.*) приш(?:ел|ла|ло|ли) (?:c )?(?:востока|запада|севера|юга|сверху|снизу).".r

  private def filterFakes(mobStrings: Seq[String]): Seq[String] = mobStrings
    .filterNot(StandupPattern.findFirstIn(_).isDefined)
    .filterNot(FightPattern.findFirstIn(_).isDefined)
    .filterNot(PetMovePattern.findFirstIn(_).isDefined)
    .filterNot(_.startsWith("*"))
}
