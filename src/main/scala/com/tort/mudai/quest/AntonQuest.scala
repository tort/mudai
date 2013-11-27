package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper._
import com.tort.mudai.person._
import akka.util.Timeout
import scala.concurrent.duration._
import Mob._
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.StartQuest
import com.tort.mudai.person.RawRead
import com.tort.mudai.person.RoamZone
import com.tort.mudai.person.StartQuest

class AntonQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper with ReachabilityHelper {

  import context._

  implicit val timeout = Timeout(5 seconds)

  val zone = persister.zoneByName(Zone.name("Антон"))
  val quester = fullName("Сельский староста занимается своими делами, что-то бурча себе под нос.")
  val questerLocation = persister.locationByMob(quester).head
  val magicFlowerLocation = persister.locationByItem(Item.fullName("Красивый цветок растет у ручья.")).head
  val forestHut = persister.locationByMob(fullName("Старый горбатый дед, варит какое-то зелье, шепча что-то себе под нос.")).head
  val forgery = persister.locationByMob("Сельский кузнец стоит тут, вздыхая.").head
  val lynxMaidLocation = persister.locationByMob(fullName("Странная девушка смотрит на вас, не мигая, зелеными, как у кошки, глазами.")).head

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      println("### QUEST STARTED")
      person ! RequestPulses

      goAndDo(questerLocation, person, (l) => {
        become(waitPrompt)
      })
  }

  def waitPrompt: Receive = {
    case RawRead(text) if text.matches("(?ms).*Сельский староста сказал : 'А то ведь не дело, село и без кузни.'.*") =>
      person ! new SimpleCommand("г помогу")
      become(waitAdvice)
  }

  def waitAdvice: Receive = {
    case RawRead(text) if text.matches("(?ms).*Сельский староста сказал : 'Сходи к нему, может, что и подскажет.'.*") =>
      person ! RoamZone(zone.name)
      become(waitFinish)
  }

  def waitFinish: Receive = {
    case RoamingFinished =>
      goAndDo(magicFlowerLocation, person, (l) => {
        person ! new SimpleCommand("взять цвет")
        goAndDo(forestHut, person, (l) => {
          become(waitHermitPrompt)
        })
      })
  }

  def waitHermitPrompt: Receive = {
    case RawRead(text) if text.matches("(?ms).*Отшельник сказал : 'Людская молва идет, что оборотней лечат чесноком... но это лишь слухи'.*") =>
      person ! new SimpleCommand("дать цвет отшел")
      become(waitPotion)
  }

  private def waitPotion: Receive = {
    case RawRead(text) if text.matches("(?ms).*Отшельник дал вам отвар..*") =>
      goAndDo(questerLocation, person, (l) => {
        person ! new SimpleCommand("дать отвар старост")
        become(waitReward)
      })
  }

  private def waitReward: Receive = {
    case RawRead(text) if text.matches("(?ms).*Сельский староста дал вам мешок мелких монет..*") =>
      extractCoins

      goAndDo(forgery, person, (l) => {
        giveIronToForger()
      })
  }


  private def extractCoins {
    person ! new SimpleCommand("брос мешок")
    person ! new SimpleCommand("взять монет")
  }

  private def giveIronToForger(time: Int = 0) {
    if (time > 2) {
      goAndDo(lynxMaidLocation, person, (l) => {
        person ! new SimpleCommand("дать чеснок девуш")
        become(waitLynxMaidReward)
      })
    } else {
      person ! new SimpleCommand("дать желез кузн")
      become(waitForgerResponse(time))
    }
  }

  private def waitLynxMaidReward: Receive = {
    case RawRead(text) if text.matches("(?ms).*Странная девушка сказала : 'А теперь прощайте! Мне надо идти..'.*") =>
      extractCoins
      finishQuest(person)
  }

  private def waitForgerResponse(times: Int): Receive = {
    case RawRead(text) if text.matches("(?ms).*Сельский кузнец сказал : 'Эх не вышло.'.*") =>
      giveIronToForger(times + 1)
    case RawRead(text) if text.matches("(?ms).*Сельский кузнец сказал : 'Вот, держи. Тебе это пригодится!'.*") =>
      giveIronToForger(times + 1)
  }
}
