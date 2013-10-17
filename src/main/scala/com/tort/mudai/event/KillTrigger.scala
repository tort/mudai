package com.tort.mudai.event

import com.tort.mudai.mapper.Mob._
import scalaz.@@

class KillTrigger extends EventTrigger[KillEvent] {
  val Pattern = ("""(?ms).*\n(?:\u001B\[\d\;\dm)?([^\n]*) (?:мертв|мертва|мертвы), (?:его|ее) душа медленно подымается в небеса\..*""" +
            """Ваш опыт повысился на ([1234567890]*) (?:очка|очков?)\..*Кровушка стынет в жилах от предсмертного крика ([^\n]*)\..*"""
    ).r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(target, exp, gen) = text
    KillEvent(shortName(target), exp.toInt, genitive(gen))
  }
}

case class KillEvent(target: String @@ ShortName, exp: Int, genitive: String @@ Genitive) extends Event
