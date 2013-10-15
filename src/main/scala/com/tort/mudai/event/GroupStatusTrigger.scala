package com.tort.mudai.event

import scalaz._
import com.tort.mudai.mapper.Mob._

class GroupStatusTrigger extends EventTrigger[GroupStatusEvent] {
  val Pattern = ("(?ms).*Ваши последователи:\r?\n" +
    """Персонаж            \| Здоровье \|Рядом\| Аффект \| Положение\r?\n""" +
    """\u001B\[\d\;\d\dm([^\n]*)\u001B\[\d\;\d\dm\|\u001B\[\d\;\d\dm\s{0,}([^\s]*)[^\n]*\|\u001B\[\d\;\d\dm\s{0,}([^\s]*)[^\n]*\|[^\n]*\|([^\s]*).*""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(name, health, isNear, status) = text

    GroupStatusEvent(shortName(name.trim), health, isNear, status)
  }
}

case class GroupStatusEvent(shortName: String @@ ShortName, health: String, isNear: String, status: String) extends Event
