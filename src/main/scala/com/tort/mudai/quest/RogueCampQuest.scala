package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper._
import com.tort.mudai.mapper.{Zone, PathHelper, LocationPersister}
import com.tort.mudai.person._
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.person.RawRead
import com.tort.mudai.event.KillEvent
import com.tort.mudai.person.StartQuest
import scalaz._
import Scalaz._
import Location._
import Mob._

class RogueCampQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {

  import context._

  def receive = quest

  val keyLocation = persister.locationByItem("Странное приспособление с зажимами и винтами висит на стене.").head
  val beforeDoor = persister.loadLocation(locationId("f18809f7-40d5-4b69-ad5a-fb2dddf4a1dc"))
  val chestLocation = persister.locationByItem("Расписанный непонятными знаками ларец светится в темноте. ..блестит!").head
  val assisters = Set(
    "Мужичок-разбойничек охраняет вход в избу.",
    "Одноглазый разбойник проходит мимо.",
    "Мальчишка - сорванец выглядывает из-за кустов.",
    "Здоровый детина сидит у входа в землянку.",
    "Разбойник идет, припадая на правую ногу.",
    "Грязная лохматая псина роется в помоях."
  ).map(fullName(_)).map(name => persister.mobByFullName(name).get)

  val mainRogue = persister.mobByFullName(fullName("Высокий плечистый человек прохаживается из угла в угол.")).get
  val hostageLocation = persister.locationByMob("Богатый пленник ожидает своей участи.").head

  def quest: Receive = {
    case StartQuest =>
      println("### QUEST STARTED")
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
    person ! RoamZone(Zone.name("Притон"))
    become(waitRoamingFInish)
  }

  private def waitRoamingFInish: Receive = {
    case RoamingFinished =>
      person ! KillMobRequest(mainRogue)
      become(waitKillMainRogue)
  }


  private def waitKillMainRogue: Receive = {
    case RoamingFinished =>
      goAndDo(hostageLocation, person, (l) => {
        finishQuest(person)
        become(quest)
      })
  }
}
