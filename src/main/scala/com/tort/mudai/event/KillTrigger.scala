package com.tort.mudai.event

import com.tort.mudai.mapper.Mob._
import scalaz.@@

class KillTrigger extends EventTrigger[KillEvent] {
  val Pattern = ("""(?ms).*\n(?:\u001B\[\d\;\dm)?([^\n]*) (?:мертв[аыо]?), (?:его|ее) душа медленно подымается в небеса\..*""" +
            """Ваш опыт повысился на ([1234567890]*) (?:очк[оа]в?)\..*Кровушка стынет в жилах от предсмертного крика ([^\n]*)\..*"""
    ).r

  def matches(text: String) = Pattern.findFirstIn(text).isDefined

  def fireEvent(text: String) = {
    val Pattern(target, exp, gen) = text
    KillEvent(shortName(target), exp.toInt, genitive(gen), magic = false)
  }
}


class KillMagicMobTrigger extends EventTrigger[KillEvent] {
  val Pattern = ("""(?ms).*\n(?:\u001B\[\d\;\dm)?([^\n]*) вспыхнул[иао]? и рассыпал[аио]?с[ья] в прах\..*""" +
            """Ваш опыт повысился на ([1234567890]*) (?:очк[оа]в?)\..*Кровушка стынет в жилах от предсмертного крика ([^\n]*)\..*"""
    ).r

  def matches(text: String) = Pattern.findFirstIn(text).isDefined

  def fireEvent(text: String) = {
    val Pattern(target, exp, gen) = text
    KillEvent(shortName(target), exp.toInt, genitive(gen), magic = true)
  }
}

case class KillEvent(target: String @@ ShortName, exp: Int, genitive: String @@ Genitive, magic: Boolean) extends Event
