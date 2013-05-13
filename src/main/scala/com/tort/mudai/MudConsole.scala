package com.tort.mudai

import akka.actor.{ActorSystem, Props}
import com.tort.mudai.person._
import akka.actor.ActorDSL._
import scala.annotation.tailrec
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.Snoop
import com.tort.mudai.person.RawWrite

class MudConsole {
  val system = ActorSystem()
  val person = system.actorOf(Props(classOf[Person]))
  val writer = actor(system) {
    new Act {
      become {
        case RawRead(text) => println(text)
      }
    }
  }

  @tailrec
  final def userInputLoop() {
    val string = readLine()
    println("#>" + string)
    string match {
      case "snoop" =>
        person ! Snoop(writer)
        userInputLoop()
      case "stopsnoop" =>
        person ! StopSnoop
        userInputLoop()
      case "login" =>
        person ! Login
        userInputLoop()
      case "zap" =>
        person ! Zap
        userInputLoop()
      case "quit" =>
        println("exited")
      case line =>
        person ! RawWrite(line + '\n')
        userInputLoop()
    }
  }
}

object MudConsole extends MudConsole {
  def main(args: Array[String]) {
    userInputLoop()
  }
}
