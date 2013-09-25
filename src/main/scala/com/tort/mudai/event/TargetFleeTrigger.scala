package com.tort.mudai.event

import com.tort.mudai.mapper.Direction
import scalaz.@@

class TargetFleeTrigger extends EventTrigger[TargetFleeEvent] {
  val Pattern = ("""(?ms).*\n(.*) сбежал[ао]? (?:на )?(.*)\..*""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(mobShortName, direction) = text

    TargetFleeEvent(mobShortName, Direction(direction))
  }
}

case class TargetFleeEvent(mobShortName: String, direction: String @@ Direction) extends Event
