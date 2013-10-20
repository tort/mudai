package com.tort.mudai.event

class FleeTrigger extends EventTrigger[FleeEvent] {
  val Pattern = """(?ms).*Вы быстро убежали с поля битвы.*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = FleeEvent()
}

case class FleeEvent() extends Event
