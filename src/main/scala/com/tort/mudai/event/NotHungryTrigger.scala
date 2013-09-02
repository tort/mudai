package com.tort.mudai.event

import com.tort.mudai.person.NotHungryEvent

class NotHungryTrigger extends EventTrigger[NotHungryEvent] {
  val Pattern = (".*(?:Вы наелись\\.|Вы слишком сыты для этого \\!).*").r;

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = NotHungryEvent()
}
