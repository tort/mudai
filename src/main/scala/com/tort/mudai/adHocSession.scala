package com.tort.mudai

import akka.actor.ActorRef
import com.tort.mudai.person.{Snoop, Login}

object adHocSession {
  def apply(person: ActorRef, console: MudConsole) = {
    person ! Login
    person ! Snoop(console.writer)
    console.userInputLoop(person, Map())
  }
}
