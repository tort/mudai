package com.tort.mudai.event

class DisarmAssistantTrigger extends EventTrigger[DisarmAssistantEvent] {
  val sample = " ловко выбил старый добрый клевец из рук дружинника."
  val Pattern = """(?ms).*(?:\u001B\[\d\;\d\d?m)?([^\n]*) ловко выбил ([^\n]*) из рук (.*)\.""".r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(target, weapon, assistant) = text

    DisarmAssistantEvent(target, weapon, assistant)
  }
}

case class DisarmAssistantEvent(target: String, weapon: String, assistant: String) extends Event
