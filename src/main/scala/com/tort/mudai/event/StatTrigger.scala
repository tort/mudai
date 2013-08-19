package com.tort.mudai.event

import com.google.inject.Inject
import com.tort.mudai.task.{AbstractTask, EventDistributor}

class StatTrigger @Inject()(eventDistributor: EventDistributor) extends EventTrigger[StatEvent]{
  private val Pattern = ("(?ms).*^Вы узнали следующее:\r?\n" +
                         "(.*)\r?\n" +
                         "[^\n]*> ЪЫ$").r
  private val NamePattern = ("(?ms).*^Вы узнали следующее:\r?\n" +
                         "Предмет \"([^\n]*)\",.*" +
                         ".*\r?\n" +
                         "[^\n]*> ЪЫ$").r


  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(desc) = text
    val NamePattern(name) = text

    StatEvent(desc)
  }
}

case class StatEvent(desc: String) extends Event
