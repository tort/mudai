package com.tort.mudai.quest

import akka.actor.ActorRef
import com.tort.mudai.mapper.{PathHelper, LocationPersister}
import com.tort.mudai.person.{RoamArea, RawRead, StartQuest, QuestHelper}

class RogueForestQuest(val mapper: ActorRef, val persister: LocationPersister, val pathHelper: PathHelper, val person: ActorRef) extends QuestHelper {
  import context._

  val rogues = Set(
    "Огромных размеров человек, в грязной одежде и с огромной, увесистой дубиной в руках.",
    "Грязный человек, явно разбойничьей наружности пристально смотрит на вас.",
    "Здоровенный детина, пристально смотрит на вас.",
    "Пьяный разбойник неспешно бредет куда-то."
  ).map(persister.mobByFullName(_).get)

  val roguesHabitation = Set(
    "Неприметная тропа",
  "В землянке",
  "В лагере разбойников",
    "К шалашу",
  "В шалаше",
  "У лаза",
  "Подземный лаз"
  )

  def receive = quest

  def quest: Receive = {
    case StartQuest =>
      goAndDo(questerLocation, person, (l) => {
        become(waitQuestPrompt)
      })
  }

  def waitQuestPrompt: Receive = {
    case RawRead(text) if text.matches("(?ms).*Глава поселения сказал : 'А инструменты ежели обнаружите - уж мне принесите, я кузнецу передам...'.*") =>
      person ! RoamArea(rogues,
  }
}
