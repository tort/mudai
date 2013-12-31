package com.tort.mudai.person

import akka.actor.ActorRef
import com.tort.mudai.mapper.Location

trait Searcher extends QuestHelper {
  protected def visit(pfProcess: (ActorRef) => Receive, onFinish: () => Unit)(toVisit: Set[Location]) {
    toVisit.toList match {
      case Nil =>
        onFinish()
      case x :: xs =>
        goAndDo(x, person, pfProcess = pfProcess, toDo = (visited) => {
          visit(pfProcess, onFinish)(xs.toSet -- visited)
        })
    }
  }

  protected def visitAll(onAllVisited: () => Unit, lfm: (ActorRef) => Receive)(toVisit: Set[Location]): Unit = {
    visit(lfm, onAllVisited)(toVisit)
  }

  protected def onAllVisited(caller: ActorRef) {
    caller ! SearchFinished
  }
}
