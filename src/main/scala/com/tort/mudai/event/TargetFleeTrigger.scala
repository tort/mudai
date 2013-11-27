package com.tort.mudai.event

import com.tort.mudai.mapper.Direction
import scalaz.@@
import com.tort.mudai.mapper.Mob._

class TargetFleeTrigger extends EventTrigger[TargetFleeEvent] {
  val Pattern = """(?ms).*\n(.*) сбежал[аои]? (?:на )?(.*)\..*""".r

  def matches(text: String) = Pattern.findFirstIn(text).isDefined

  def fireEvent(text: String) = {
    println("FIRE TARGET FLEE")
    val Pattern(mobShortName, direction) = text

    TargetFleeEvent(shortName(mobShortName), Direction(direction))
  }
}

case class TargetFleeEvent(mobShortName: String @@ ShortName, direction: String @@ Direction) extends Event
