package com.tort.mudai.event

class StatusLineTrigger extends EventTrigger[StatusLineEvent] {
  val Pattern = ("""(?ms).*\u001B\[\d;\d\dm(\d+)H\u001B\[0;37m \u001B\[\d;\d\dm(\d+)M\u001B\[0;37m (\d+)о Зауч:([^\s]*) (\d+)L (\d+)G Вых:[^\n]*> $""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(health, stamina, exp, mem, level, gold) = text

    StatusLineEvent(health.toInt, stamina.toInt, exp.toInt, seconds(mem), level.toInt, gold.toInt)
  }

  private def seconds(mem: String) = mem match {
    case "0" => 0
    case m =>
      val secondsAndMinutes = mem.split(":")
      secondsAndMinutes(0).toInt * 60 + secondsAndMinutes(1).toInt
  }
}

case class StatusLineEvent(health: Int, stamina: Int, exp: Int, mem: Int, level: Int, gold: Int) extends Event
