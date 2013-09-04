package com.tort.mudai.event

class TargetFleeTrigger extends EventTrigger[TargetFleeEvent] {
  val Pattern = ("""(?ms).*\n(.*) сбежал[ао]? (?:на )?(.*)\..*""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(mobShortName, direction) = text

    TargetFleeEvent(mobShortName, direction)
  }
}

case class TargetFleeEvent(mobShortName: String, direction: String) extends Event
