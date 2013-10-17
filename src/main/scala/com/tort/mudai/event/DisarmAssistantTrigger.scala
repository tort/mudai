package com.tort.mudai.event

class DisarmAssistantTrigger extends EventTrigger[DisarmAssistantEvent] {
  val Pattern = """(?ms).*([^\n]*) ловко выбил ([^\n]*) из рук ([^\n]*)\..*""".r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(target, weapon, assistant) = text

    DisarmAssistantEvent(target, weapon, assistant)
  }
}

case class DisarmAssistantEvent(target: String, weapon: String, assistant: String) extends Event
