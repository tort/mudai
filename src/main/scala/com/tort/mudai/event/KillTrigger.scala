package com.tort.mudai.event

import com.tort.mudai.mapper.Mob._
import scalaz.@@

class KillTrigger extends EventTrigger[KillEvent] {
  val Pattern = ("""(?ms).*\n(?:\u001B\[\d\;\dm)?([^\n]*) (?:мертв[аыо]?), (?:его|ее) душа медленно подымается в небеса\..*""" +
            """Ваш опыт повысился на ([1234567890]*) (?:очка|очков?)\..*Кровушка стынет в жилах от предсмертного крика ([^\n]*)\..*"""
    ).r

  val sample = "Дружинник изрубил дикую яблоньку. От дикой яблоньки осталось только кровавое месиво.\nДикая яблонька вспыхнула и рассыпалась в прах.\nВаш опыт повысился на 7000 очков.\nКровушка стынет в жилах от предсмертного крика дикой яблоньки.\n\n378H 228M 529398о Зауч:0 23L 62G Вых:СВЮЗ> "

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(target, exp, gen) = text
    KillEvent(shortName(target), exp.toInt, genitive(gen))
  }
}

case class KillEvent(target: String @@ ShortName, exp: Int, genitive: String @@ Genitive) extends Event
