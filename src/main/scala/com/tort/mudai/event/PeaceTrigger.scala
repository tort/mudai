package com.tort.mudai.event

class PeaceTrigger extends EventTrigger[PeaceStatusEvent] {
  val Pattern = ("""(?ms).*\n[^\n\]]*>[^\n]*""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    PeaceStatusEvent()
  }
}

case class PeaceStatusEvent() extends Event
