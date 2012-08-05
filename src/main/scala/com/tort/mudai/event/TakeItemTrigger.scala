package com.tort.mudai.event

import com.google.inject.Inject
import com.tort.mudai.Handler
import com.tort.mudai.task.{AbstractTask, EventDistributor}

class TakeItemTrigger @Inject()(eventDistributor: EventDistributor) extends EventTrigger[TakeItemEvent] {
  private val EventPattern = ("^Вы подняли ([^\n])\\..*").r

  def matches(text: String) = text.matches(EventPattern.toString())

  def fireEvent(text: String) = {
    val EventPattern(item) = text

    eventDistributor.invoke(new Handler(){
      def handle(task: AbstractTask) = task.takeItem(item)
    })

    TakeItemEvent(item)
  }
}


