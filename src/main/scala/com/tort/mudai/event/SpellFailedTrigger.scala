package com.tort.mudai.event

import scalaz.@@
import com.tort.mudai.person.Spell
import Spell._

class SpellFailedTrigger extends EventTrigger[SpellFailedEvent]{
  val Pattern = """(?ms).*Вы произнесли заклинание "\u001B\[\d\;\d\dm([^\n]*)\u001B\[\d\;\d\dm"\..*Ваши потуги оказались напрасными\..*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(spellText) = text

    SpellFailedEvent(spell(spellText))
  }
}

case class SpellFailedEvent(spell: String @@ Spell) extends Event

class CurseSucceededTrigger extends EventTrigger[CurseSucceededEvent] {
  val Pattern = """(?ms).*Красное сияние вспыхнуло над (.*) и тут же погасло!.*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(target) = text
    CurseSucceededEvent(target)
  }
}

case class CurseSucceededEvent(target: String) extends Event

class LongHoldSucceededTrigger extends EventTrigger[LongHoldSucceededEvent] {
  val Pattern = """(?ms).*Вы произнесли заклинание "длительное оцепенение".
                  |Вы занесли заклинание "длительное оцепенение" в свои резы.
                  |([^\n]*) замер на месте!.*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(target) = text

    LongHoldSucceededEvent(target)
  }
}

case class LongHoldSucceededEvent(target: String) extends Event
