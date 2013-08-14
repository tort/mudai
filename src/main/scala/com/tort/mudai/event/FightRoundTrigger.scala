package com.tort.mudai.event

class FightRoundTrigger extends EventTrigger[FightRoundEvent] {
  //TODO extract name
  val Pattern = ("""(?ms).*\[Веретень:([^\]]*)\].*\[([^\]]*):([^\]]*)].*""").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(state, target, targetState) = text

    FightRoundEvent(state, target, targetState)
  }
}

case class FightRoundEvent(state: String, target: String, targetState: String) extends Event