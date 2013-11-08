package com.tort.mudai.event

import scalaz.@@
import com.tort.mudai.mapper.Mob
import Mob._

class FightRoundTrigger extends EventTrigger[FightRoundEvent] {
  //TODO extract name
  val Pattern = ("""(?ms).*\[Веретень:([^\]]*)\][^\]]*\[([^\]]*):((?:Невредим|Слегка|Легко|Ранен|Тяжело|О.тяжело)[^\]]{0,})][^\]]*>.*""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(state, target, targetState) = text

    val firstLetter = target.take(1).toUpperCase
    FightRoundEvent(state, shortName(firstLetter + target.drop(1)), targetState)
  }
}

case class FightRoundEvent(state: String, target: String @@ ShortName, targetState: String) extends Event
