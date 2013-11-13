package com.tort.mudai.event

class SpellFailedTrigger extends EventTrigger[SpellFailedEvent]{
  val Pattern = """(?ms).*Вы произнесли заклинание "\u001B\[\d\;\d\dm([^\n]*)\u001B\[\d\;\d\dm"\..*Ваши потуги оказались напрасными\..*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(spell) = text

    SpellFailedEvent(spell)
  }
}

case class SpellFailedEvent(spell: String) extends Event

class CurseSucceededTrigger extends EventTrigger[CurseSucceededEvent] {
  val Pattern = """(?ms).*Красное сияние вспыхнуло над (.*) и тут же погасло!.*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(target) = text
    CurseSucceededEvent(target)
  }
}

case class CurseSucceededEvent(target: String) extends Event


