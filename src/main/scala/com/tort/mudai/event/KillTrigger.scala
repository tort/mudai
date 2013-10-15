package com.tort.mudai.event

import com.tort.mudai.mapper.Mob._
import scalaz.@@

class KillTrigger extends EventTrigger[KillEvent] {
  val Pattern = ("""(?ms).*(?:\u001B\[\d\;\dm)?([^\n]*) (?:мертв|мертва|мертвы), (?:его|ее) душа медленно подымается в небеса\..*""" +
            """Ваш опыт повысился на ([1234567890]*) (?:очка|очков?)\..*"""
    ).r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    println("FIRE KILL")
    val Pattern(target, exp) = text
    KillEvent(shortName(target), exp.toInt)
  }
}

case class KillEvent(target: String @@ ShortName, exp: Int) extends Event
