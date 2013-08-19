package com.tort.mudai

import akka.actor.{Props, ActorSystem}
import com.tort.mudai.person.Person
import com.tort.mudai.mapper.{MudMapper, PathHelper, SQLLocationPersister}

object Runner {
  def main(args: Array[String]) {
    val system = ActorSystem()
    val persister = new SQLLocationPersister
    val pathHelper = new PathHelper(persister)
    val mapper = system.actorOf(Props(classOf[MudMapper], pathHelper, persister, persister))
    val person = system.actorOf(Props(classOf[Person], args(0), args(1), mapper, pathHelper, persister))
    adHocSession(person, new MudConsole)
  }
}
