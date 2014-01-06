package com.tort.mudai.event

class TellTrigger extends EventTrigger[TellEvent] {
  private val Pattern = """(?ms).*([^\n]*) сказал[аои]? вам : '(?:[^']*)'.*""".r

  def matches(text: String) = Pattern.findFirstIn(text).isDefined

  def fireEvent(text: String) = {
    val Pattern(who) = text

    TellEvent(who)
  }
}

case class TellEvent(who: String) extends Event
