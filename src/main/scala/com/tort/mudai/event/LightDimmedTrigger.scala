package com.tort.mudai.event

class LightDimmedTrigger extends EventTrigger[LightDimmedEvent] {
  val Pattern = ("""(?ms).*Ваш.? ([^\n]*) замерцал.? и начал.? угасать\..*""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    LightDimmedEvent()
  }
}

case class LightDimmedEvent() extends Event
