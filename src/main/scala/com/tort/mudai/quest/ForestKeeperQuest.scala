package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper.{Mob, PathHelper, LocationPersister}
import com.tort.mudai.person._
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.event.KillEvent
import Mob._
import scalaz._
import Scalaz._

class ForestKeeperQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {
  import context._

  def receive = quest

  val anthill = persister.locationByItem("Огромный муравейник прилегает к южной стороне дерева.").head

  val forestKeeperFullName = fullName("Хозяин леса аж трясется от ярости, заметив Вас.")
  val forestKeeperLocation = persister.locationByMob(forestKeeperFullName).head

  val forestKeeper = persister.mobByFullName(forestKeeperFullName).head
  val quester = persister.locationByMob("Девочка-припевочка сегодня почему-то очень грустна.").head

  def quest: Receive = {
    case StartQuest =>
      println("### QUEST STARTED")
      person ! RequestPulses

      goAndDo(anthill, person, (l) => {
        become(waitTakeEggs)
        person ! new SimpleCommand("взять все муравейник")
      })
  }

  def waitTakeEggs: Receive = {
    case RawRead(text) if text.matches("(?ms).*Вы взяли муравьиные яйца из муравейника..*") =>
      goAndDo(forestKeeperLocation, person, (l) => {
        person ! new SimpleCommand("брос яйца")
        become(waitKill)
      })
    case RawRead(text) if text.matches("""(?ms).*Муравейник пуст\..*""") =>
      println("### QUEST NOT RESPONDING")
      finishQuest(person)
      become(quest)
  }

  import Mob._
  def waitKill: Receive = {
    case KillEvent(shortName, _, _, _) if shortName === forestKeeper.shortName.get =>
      goAndDo(quester, person, (l) => {
        person ! new SimpleCommand("дать камень девочка")
        finishQuest(person)
        become(quest)
      })
  }
}
