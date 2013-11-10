package com.tort.mudai.event

class OrderFailedTrigger extends EventTrigger[OrderFailedEvent] {
  val Pattern = """(?ms)Ладушки\.\r\n\r\n[^\n]*>.*"""

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    OrderFailedEvent()
  }
}

case class OrderFailedEvent() extends Event
