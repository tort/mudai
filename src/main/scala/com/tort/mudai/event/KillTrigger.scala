package com.tort.mudai.event

class KillTrigger extends EventTrigger[KillEvent] {
  val Pattern = ("""(?ms).*\u001B\[\d\;\dm([^\n]*) (?:мертв|мертва|мертвы), (?:его|ее) душа медленно подымается в небеса\..*""" +
            """Ваш опыт повысился на ([1234567890]*) (?:очка|очков?)\.\r?\n.*"""
    ).r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(target, exp) = text
    KillEvent(target, exp.toInt)
  }
}

case class KillEvent(target: String, exp: Int) extends Event
