package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper.{PathHelper, LocationPersister}
import com.tort.mudai.person.{RawRead, StartQuest, RequestPulses, QuestHelper}
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.event.KillEvent

class RogueCampQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {
  import context._

  def receive = quest

  val keyLocation = persister.locationByItem("Странное приспособление с зажимами и винтами висит на стене.").head
  val beforeDoor = persister.loadLocation("8596c4b9-f6f8-4441-96b8-b7e5c2308022")
  val chestLocation = persister.locationByItem("Расписанный непонятными знаками ларец светится в темноте. ..блестит!").head

  def quest: Receive = {
    case StartQuest =>
      println("QUEST STARTED")
      person ! RequestPulses

      goAndDo(keyLocation, person, (l) => {
        become(waitTakeKey)
        person ! new SimpleCommand("взять все приспособление")
      })
  }

  def waitTakeKey: Receive = {
    case RawRead(text) if text.matches("""(?ms).*Вы взяли ключ от сокровищницы из странного приспособления\..*""") =>
      goAndDo(beforeDoor, person, (l) => {
        person ! new SimpleCommand("отпереть дверь")
        goAndDo(chestLocation, person, (l) => {
          person ! new SimpleCommand("откр ларец")
          person ! new SimpleCommand("взять все ларец")
          become(waitKill)
        })
      })
    case RawRead(text) if text.matches("""(?ms).*Странное приспособление пусто.*""") =>
      finishQuest(person)
      become(quest)
  }

  def waitKill: Receive = {
    case KillEvent(_, _, _, _) =>
      finishQuest(person)
      become(quest)
  }
}
