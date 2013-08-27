package com.tort.mudai.event

class MemFinishedTrigger extends EventTrigger[MemFinishedEvent]{
  val Pattern = ("(?ms).*Наконец ваши занятия окончены. Вы с улыбкой убрали свои резы\\..*").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    MemFinishedEvent()
  }
}

case class MemFinishedEvent() extends Event
