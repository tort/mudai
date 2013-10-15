package com.tort.mudai.event

class CurseFailedTrigger extends EventTrigger[CurseFailedEvent]{
  val Pattern = """(?ms).*Вы произнесли заклинание "\u001B\[\d\;\d\dmпроклятье\u001B\[\d\;\d\dm"\..*Ваши потуги оказались напрасными\..*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    println("CURS FAILE TRIGGER")
    CurseFailedEvent()
  }
}

case class CurseFailedEvent() extends Event

class CurseSucceededTrigger extends EventTrigger[CurseSucceededEvent] {
  val Pattern = """(?ms).*Красное сияние вспыхнуло над (.*) и тут же погасло!.*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(target) = text
    CurseSucceededEvent(target)
  }
}

case class CurseSucceededEvent(target: String) extends Event


