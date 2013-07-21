package com.tort.mudai.event

trait EventTrigger[+T <: Event] extends Trigger {
  def fireEvent(text: String): T
}
