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

case class SpellFailedEvent(spell: String @@ SpellName) extends Event

class CurseSucceededTrigger extends EventTrigger[SpellSucceededEvent] {
  val Pattern = """(?ms).*Красное сияние вспыхнуло над (.*) и тут же погасло!.*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(target) = text
    SpellSucceededEvent(target, Curse)
  }
}

class LongHoldSucceededTrigger extends EventTrigger[SpellSucceededEvent] {
  val Pattern = """(?ms).*Вы произнесли заклинание "длительное оцепенение".
                  |Вы занесли заклинание "длительное оцепенение" в свои резы.
                  |([^\n]*) замер на месте!.*""".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(target) = text

    SpellSucceededEvent(target, LongHold)
  }
}

case class SpellSucceededEvent(target: String, spell: String @@ SpellName) extends Event
