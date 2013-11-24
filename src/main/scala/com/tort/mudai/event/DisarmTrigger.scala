package com.tort.mudai.event

class DisarmTrigger extends EventTrigger[DisarmEvent] {
  val Pattern = ("""(?ms).* ловко выбил[ао]? (.*) из ваших рук\..*""").r

  def matches(text: String) = Pattern.findFirstIn(text).isDefined

  def fireEvent(text: String) = {
    val Pattern(what) = text

    DisarmEvent("", what)
  }
}

case class DisarmEvent(who: String, what: String) extends Event
