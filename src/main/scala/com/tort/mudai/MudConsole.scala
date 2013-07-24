package com.tort.mudai

import akka.actor.{ActorRef, ActorSystem, Props}
import com.tort.mudai.person._
import akka.actor.ActorDSL._
import scala.annotation.tailrec
import com.tort.mudai.person.Snoop
import com.tort.mudai.person.RawWrite

class MudConsole {
  def writer(text: String) = println(text)

  @tailrec
  final def userInputLoop(person: ActorRef) {
    val string = readLine()
    println("#>" + string)
    string match {
      case "snoop" =>
        person ! Snoop(writer)
        userInputLoop(person)
      case "stopsnoop" =>
        person ! StopSnoop
        userInputLoop(person)
      case "login" =>
        person ! Login
        userInputLoop(person)
      case "zap" =>
        person ! Zap
        userInputLoop(person)
      case "quit" =>
        println("exited")
      case line =>
        person ! RawWrite(line + '\n')
        userInputLoop(person)
    }
  }
}
