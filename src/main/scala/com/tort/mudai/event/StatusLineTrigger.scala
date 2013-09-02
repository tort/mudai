package com.tort.mudai.event

class StatusLineTrigger extends EventTrigger[StatusLineEvent] {


  val Pattern = ("""(?ms).*\u001B\[\d;\d\dm(\d+)H\u001B\[0;37m \u001B\[\d;\d\dm(\d+)M\u001B\[0;37m (\d+)о Зауч:[^\s]* (\d+)L (\d+)G Вых:[^\n]*> $""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(health, stamina, exp, level, gold) = text

    StatusLineEvent(health.toInt, stamina.toInt, exp.toInt, level.toInt, gold.toInt)
  }
}

case class StatusLineEvent(health: Int, stamina: Int, exp: Int, level: Int, gold: Int) extends Event
