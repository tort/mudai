package com.tort.mudai

import akka.actor.{Props, ActorSystem}
import com.tort.mudai.person.Person

object Runner {
  def main(args: Array[String]) {
    val system = ActorSystem()
    val person = system.actorOf(Props(classOf[Person], args(0), args(1)))
    adHocSession(person, new MudConsole)
  }
}
