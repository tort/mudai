package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper.{Mob, Zone, PathHelper, LocationPersister}
import com.tort.mudai.person._
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.person.RawRead
import com.tort.mudai.event.KillEvent
import com.tort.mudai.person.StartQuest

class RogueCampQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

  def receive = quest

  val keyLocation = persister.locationByItem("Странное приспособление с зажимами и винтами висит на стене.").head
  val beforeDoor = persister.loadLocation("8596c4b9-f6f8-4441-96b8-b7e5c2308022")
  val chestLocation = persister.locationByItem("Расписанный непонятными знаками ларец светится в темноте. ..блестит!").head
  val assisters = Set(
    "Мужичок-разбойничек охраняет вход в избу.",
    "Одноглазый разбойник проходит мимо.",
    "Мальчишка - сорванец выглядывает из-за кустов.",
    "Здоровый детина сидит у входа в землянку.",
    "Разбойник идет, припадая на правую ногу.",
    "Грязная лохматая псина роется в помоях."
  ).map(name => persister.mobByFullName(name).get)

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
    case RawRead(text) if text.matches( """(?ms).*Вы взяли ключ от сокровищницы из странного приспособления\..*""") =>
      goAndDo(beforeDoor, person, (l) => {
        person ! new SimpleCommand("отпереть дверь")
        goAndDo(chestLocation, person, (l) => {
          person ! new SimpleCommand("откр ларец")
          person ! new SimpleCommand("взять все ларец")
          become(waitKillSpirit)
        })
      })
    case RawRead(text) if text.matches( """(?ms).*Странное приспособление пусто.*""") =>
      killAssisters
  }

  def waitKillSpirit: Receive = {
    case KillEvent(_, _, _, _) =>
      killAssisters
  }

  private def killAssisters {
    person ! Roam(Zone.name("Разбойничий лагерь"))
//    become(waitRoamingFInish)
  }

//  private def waitRoamingFInish: Receive = {
//    case RoamingFinished =>
//      person ! KillMobRequest(mainRogue)
//      become(waitKillMainRogue)
//  }

  import Mob._
//  private def waitKillMainRogue: Receive = {
//    case KillEvent(shortName, _, _, _) if shortName === mainRogue.shortName.get =>
//      goAndDo()
//  }
}
