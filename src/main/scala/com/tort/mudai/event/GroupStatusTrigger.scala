package com.tort.mudai.event

class GroupStatusTrigger extends EventTrigger[GroupStatusEvent] {
  val Pattern = ("(?ms).*Ваши последователи:\r?\n" +
    """Персонаж            \| Здоровье \|Рядом\| Аффект \| Положение\r?\n.*""" +
    """\u001B\[\d\;\d\dm([^\s]*)[^\n]*\|\u001B\[\d\;\d\dm\s{0,}([^\s]*)[^\n]*\|\u001B\[\d\;\d\dm\s{0,}([^\s]*)[^\n]*\|[^\n]*\|([^\s]*).*""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(name, health, isNear, status) = text
    println("GROUP STATUS FIRED")

    GroupStatusEvent(status)
  }
}

case class GroupStatusEvent(mobStatus: String) extends Event
