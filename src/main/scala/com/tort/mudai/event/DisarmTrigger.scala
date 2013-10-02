package com.tort.mudai.event

class DisarmTrigger extends EventTrigger[DisarmEvent] {
  val Pattern = ("""(?ms).* ловко выбил[ао]? (.*) из ваших рук\..*""").r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    println("DISARM")
    val Pattern(what) = text

    DisarmEvent("", what)
  }
}

case class DisarmEvent(who: String, what: String) extends Event
