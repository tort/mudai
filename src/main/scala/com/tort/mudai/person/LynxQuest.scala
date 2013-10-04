package com.tort.mudai.person

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.tort.mudai.mapper._
import akka.actor.{Props, ActorRef}
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event.KillEvent

class LynxQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  val searcher = context.actorOf(Props(classOf[Searcher], mapper, persister, pathHelper, person))

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
              val mobs = Set(
                "Пятнистая рысь изогнула спину перед прыжком здесь.",
                "Бобер строит здесь запруду."
              )
              searcher ! FindMobs(mobs)

              become(waitMobFound(l))
          }
      }
  }

  def waitMobFound(startLocation: Location): Receive = {
    case MobFound(alias) =>
      person ! Attack(alias)
      become(waitKill(startLocation))
    case SearchFinished =>
      goAndDo(hunterLocation, person, (visited) => {
        (0 to 7).foreach(i => person ! new SimpleCommand("дать труп охот"))
        goRentAndFinishQuest(startLocation, person)
      })
    case e => searcher ! e
  }

  def waitKill(startLocation: Location): Receive = {
    case KillEvent(_, _) =>
      person ! new SimpleCommand("взять все все.труп")
      person ! new SimpleCommand("взять все.труп")
      become(waitMobFound(startLocation))
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
