package com.tort.mudai

import akka.actor.ActorRef
import akka.pattern.ask
import scalaz._
import Scalaz._
import com.tort.mudai.person._
import akka.actor.ActorDSL._
import scala.annotation.tailrec
import com.tort.mudai.person.Snoop
import com.tort.mudai.person.RawWrite
import com.tort.mudai.mapper.{Location, SQLLocationPersister, PathTo}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import akka.util.Timeout
import scala.concurrent.duration._

class MudConsole {
  val persister = new SQLLocationPersister

  def writer(text: String) = println(text)

  implicit val timeout = Timeout(5 seconds)

  @tailrec
  final def userInputLoop(person: ActorRef, menuMap: Map[Int, Location]) {
    val line = readLine()
    println("#>" + line)
    val splitted: Seq[String] = line.split(' ').toList
    splitted match {
      case "snoop" :: Nil =>
        person ! Snoop(writer)
        userInputLoop(person, Map())
      case "stopsnoop" :: Nil =>
        person ! StopSnoop
        userInputLoop(person, Map())
      case "login" :: Nil =>
        person ! Login
        userInputLoop(person, Map())
      case "zap" :: Nil =>
        person ! com.tort.mudai.person.Zap
        userInputLoop(person, Map())
      case "quit" :: Nil =>
        println("exited")
      case menuIndex :: Nil if (!menuMap.isEmpty && menuIndex.forall(_.isDigit)) =>
        val index = menuIndex.toInt
        val loc = menuMap(index)
        val future: Future[List[String]] = for {
          f <- (person ? PathTo(loc)).mapTo[List[String]]
        } yield f

        future onSuccess {
          case path => path.mkString
        }
        userInputLoop(person, Map())
      case "путь" :: target :: Nil =>
        persister.locationByTitle(target) match {
          case Nil =>
            writer("### Location not found")
            none
            userInputLoop(person, Map())
          case loc :: Nil =>
            loc.some
            userInputLoop(person, Map())
          case locs =>
            val map = locs.zipWithIndex.map(x => x.swap)
            map.foreach(x => writer("%s. %s".format(x._1, x._2.title)))
            userInputLoop(person, map.toMap)
        }
      case _ =>
        person ! RawWrite(line + '\n')
        userInputLoop(person, Map())
    }
  }
}
