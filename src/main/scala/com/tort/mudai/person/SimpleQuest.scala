package com.tort.mudai.person

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.mapper._
import akka.actor._
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event.KillEvent
import akka.actor.Terminated

class SimpleQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      println("QUEST STARTED")
      person ! RequestPulses

      val future = for {
        f <- (mapper ? CurrentLocation).mapTo[Option[Location]]
      } yield f

      future onSuccess {
        case current =>
          current.foreach {
            case l =>
              hirePart(l)
          }
      }
  }

  private def hirePart(location: Location) {
    val searcher = context.actorOf(Props(classOf[Searcher], mapper, persister, pathHelper, person))
    searcher ! FindMobs(Set("Батрак работает здесь.").map(n => persister.mobByFullName(n).get))//TODO refactor
    become(waitMobFound(searcher, location))
  }

  private def waitMobFound(searcher: ActorRef, location: Location): Receive = {
    case MobFound(_) =>
      person ! new SimpleCommand("нанять батрак 10")
      watch(searcher)
      searcher ! PoisonPill
      become {
        case Terminated(ref) if ref == searcher =>
          zoningPart(location)
      }
    case SearchFinished =>
      println("### MOB TO HIRE NOT FOUND")
      goRentAndFinishQuest(location, person)
    case e => searcher ! e
  }

  private def zoningPart(l: Location) {
    val mobs = Set(
      "Пятнистая рысь изогнула спину перед прыжком здесь.",
      "Бобер строит здесь запруду.",
      "Волк готовится к нападению здесь."
    )

    val searcher = context.actorOf(Props(classOf[Searcher], mapper, persister, pathHelper, person))
    searcher ! FindMobs(mobs.map(n => persister.mobByFullName(n).get))

    become(waitTargetFound(searcher, l))
  }

  def waitTargetFound(searcher: ActorRef, startLocation: Location): Receive = {
    case MobFound(alias) =>
      person ! Attack(alias)
      become(waitKill(searcher, startLocation))
    case SearchFinished =>
      goAndDo(hunterLocation, person, (visited) => {
        (0 to 7).foreach(i => person ! new SimpleCommand("дать труп охот"))
        goRentAndFinishQuest(startLocation, person)
      })
    case e => searcher ! e
  }

  def waitKill(searcher: ActorRef, startLocation: Location): Receive = {
    case KillEvent(_, _) =>
      person ! new SimpleCommand("взять все все.труп")
      person ! new SimpleCommand("взять все.труп")
      become(waitTargetFound(searcher, startLocation))
    case Pulse =>
    case e => searcher ! e
  }

  def goRentAndFinishQuest(startLocation: Location, person: ActorRef) {
    goAndDo(startLocation, person, (visited) => {
      person ! YieldPulses
      unbecome()
      finishQuest(person)
    })
  }

  def hunterLocation: Location = persister.locationByTitle("Жилище охотника").head
}
