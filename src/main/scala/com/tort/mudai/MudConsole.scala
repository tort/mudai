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
import com.tort.mudai.mapper._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.Snoop
import com.tort.mudai.mapper.PathTo
import com.tort.mudai.person.GoTo
import com.tort.mudai.person.RawWrite
import com.tort.mudai.person.Roam
import com.tort.mudai.mapper.NameZone
import com.tort.mudai.person.Attack

class MudConsole {
  val persister = new SQLLocationPersister

  def writer(text: String) = println(text)

  implicit val timeout = Timeout(5 seconds)

  val quests = Set("бобры", "белый паук", "рысь", "глава", "угодья", "хозяин леса", "лагерь разбойников", "инструмент кузнеца", "дятлы", "страшная сказка")

  @tailrec
  final def userInputLoop(person: ActorRef, menuMap: Map[Int, Location]) {
    val line = readLine()
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
      case "путь" :: menuIndex :: Nil if (!menuMap.isEmpty && menuIndex.forall(_.isDigit)) =>
        menuIndex.toInt |> menuMap |> showPath(person)
        userInputLoop(person, Map())
      case "путь" :: target :: Nil =>
        val targetAndMenu = menu(target)
        targetAndMenu._1.foreach(showPath(person))
        userInputLoop(person, targetAndMenu._2)
      case "го" :: menuIndex :: Nil if (!menuMap.isEmpty && menuIndex.forall(_.isDigit)) =>
        menuIndex.toInt |> menuMap |> goTo(person)
        userInputLoop(person, Map())
      case "го" :: target :: Nil =>
        val targetAndMenu = menu(target)
        targetAndMenu._1.foreach(person ! GoTo(_))
        userInputLoop(person, targetAndMenu._2)
      case "зонинг" :: zoneName =>
        person ! Roam(Zone.name(zoneName.mkString(" ")))
        userInputLoop(person, Map())
      case "стопзонинг" :: Nil =>
        person ! InterruptRoaming
        userInputLoop(person, Map())
      case "зона" :: name =>
        person ! NameZone(Zone.name(name.mkString(" ")))
        userInputLoop(person, Map())
      case "аттак" :: name =>
        val targetName: String = name.mkString(" ")
        person ! Attack(targetName)
        userInputLoop(person, Map())
      case "квест" :: name =>
        val questName: String = name.mkString(" ")
        if (!quests(questName))
          quests.foreach(println)
        else
          person ! StartQuest(questName)
        userInputLoop(person, Map())
      case "проверка" :: Nil =>
        person ! CheckUnreachable
        userInputLoop(person, Map())
      case _ =>
        person ! RawWrite(line + '\n')
        userInputLoop(person, Map())
    }
  }

  private def goTo(person: ActorRef)(loc: Location) = person ! GoTo(loc)

  private def showPath(person: ActorRef)(loc: Location) {
    val future: Future[List[String]] = for {
      f <- (person ? PathTo(loc)).mapTo[List[String]]
    } yield f

    future onSuccess {
      case path => path.mkString
    }
  }

  private def menu(target: String): (Option[Location], Map[Int, Location]) = {
    persister.locationByTitle(target) match {
      case Nil =>
        writer("### Location not found")
        (none, Map())
      case loc :: Nil =>
        (loc.some, Map())
      case locs =>
        val menuItems = locs.zipWithIndex.map(x => x.swap)
        menuItems.foreach(x => writer("%s. %s - %s".format(x._1, x._2.title, x._2.zone.map(_.name).getOrElse("no zone"))))
        (none, menuItems.toMap)
    }
  }
}
