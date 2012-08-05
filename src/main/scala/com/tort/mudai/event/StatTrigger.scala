package com.tort.mudai.event

import com.google.inject.Inject
import com.tort.mudai.task.{AbstractTask, EventDistributor}
import com.tort.mudai.Handler
import com.tort.mudai.persistance.Stat

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

    eventDistributor.invoke(new Handler(){
      def handle(task: AbstractTask) = task.viewStat(new Stat(name, desc))
    })

    StatEvent(desc)
  }
}

case class StatEvent(desc: String) extends Event
