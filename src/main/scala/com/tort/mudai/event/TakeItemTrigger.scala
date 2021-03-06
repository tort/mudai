package com.tort.mudai.event

class TakeItemTrigger extends EventTrigger[TakeItemEvent] {
  private val EventPattern = ("^Вы подняли ([^\n])\\..*").r

  def matches(text: String) = text.matches(EventPattern.toString())

  def fireEvent(text: String) = {
    val EventPattern(item) = text

    TakeItemEvent(item)
  }
}


