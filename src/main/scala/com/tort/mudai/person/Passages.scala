package com.tort.mudai.person

import akka.actor.{ActorRef, Actor}
import com.tort.mudai.command.SimpleCommand
import com.tort.mudai.mapper.{LocationPersister, MoveEvent}
import scalaz._
import Scalaz._
import com.tort.mudai.event.StatusLineEvent

class Passages(persister: LocationPersister, person: ActorRef) extends Actor {
  def receive = rec(0)

  def rec(level: Int): Receive = {
    case StatusLineEvent(_, _, _, _, lvl, _) =>
      if (level != lvl)
        context.become(rec(lvl))
    case r@TriggeredMoveRequest("У шалаша", direction, "Тихий угол") =>
      sender ! new SimpleCommand(s"дать $level кун следопыт")
      person ! MoveEvent(
        persister.locationByTitle(r.from).headOption,
        direction,
        persister.locationByTitle(r.to).head)
    case r@TriggeredMoveRequest("Тихий угол", direction, "У шалаша") =>
      sender ! new SimpleCommand(s"дать $level кун следопыт")
      person ! MoveEvent(
        persister.locationByTitle(r.from).headOption,
        direction,
        persister.locationByTitle(r.to).head)
    case TriggeredMoveRequest("На лугу", direction, "На лугу") if direction == "v1_v2_trigger" =>
      sender ! new SimpleCommand(s"дать $level кун цыган")
      person ! MoveEvent(
        persister.loadLocation("a2487caf-444f-4736-978f-0f1fbbd6083d").some,
        direction,
        persister.loadLocation("ebb1d973-693f-4d3c-95f9-8b0f187f7eaa"))
    case TriggeredMoveRequest("На лугу", direction, "На лугу") if direction == "v2_v1_trigger" =>
      sender ! new SimpleCommand(s"дать $level кун цыган")
      person ! MoveEvent(
        persister.loadLocation("ebb1d973-693f-4d3c-95f9-8b0f187f7eaa").some,
        direction,
        persister.loadLocation("a2487caf-444f-4736-978f-0f1fbbd6083d"))
    case TriggeredMoveRequest("Дорога", direction, "Дорога") if direction == "trigger_swamp_south" =>
      sender ! new SimpleCommand(s"дать ${level * 2} кун болотник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Но наконец путь кончился, и Вы уже за болотами..*") =>
          person ! MoveEvent(
            persister.loadLocation("f6c1f708-c24c-41cc-b653-9ff36a64e731").some,
            direction,
            persister.loadLocation("9e0df04f-4bf2-4313-b305-fa84d574300b"))
          context.unbecome()
      }
    case TriggeredMoveRequest("Дорога", direction, "Дорога") if direction == "trigger_swamp_north" =>
      sender ! new SimpleCommand(s"дать ${level} кун болотник")
      person ! MoveEvent(
        persister.loadLocation("2b7f6585-69eb-4e9f-8d46-bb649b42ca36").some,
        direction,
        persister.loadLocation("fcb3a6b8-0393-423e-aae6-7c2335e2c3bc"))
    case TriggeredMoveRequest("У ямы", direction, "У ямы") if direction == "trigger_jump_north" =>
      sender ! new SimpleCommand("перепрыгнуть яма")
      person ! MoveEvent(
        persister.loadLocation("b29f1fc5-01f9-42ca-a7ca-662e11e8866f").some,
        direction,
        persister.loadLocation("a2fb7b2f-4830-4900-94f2-9e6dea318daf"))
    case TriggeredMoveRequest("У ямы", direction, "У ямы") if direction == "trigger_jump_south" =>
      sender ! new SimpleCommand("перепрыгнуть яма")
      person ! MoveEvent(
        persister.loadLocation("a2fb7b2f-4830-4900-94f2-9e6dea318daf").some,
        direction,
        persister.loadLocation("b29f1fc5-01f9-42ca-a7ca-662e11e8866f"))
    case r@TriggeredMoveRequest("Сокровищница", direction, "Широкий проход") if direction == "trigger_treasure_down" =>
      sender ! new SimpleCommand("г сезам откройся")
      person ! MoveEvent(
        persister.loadLocation("145016ed-b151-46d4-8db6-86dc7ae33137").some,
        direction,
        persister.loadLocation("bc745d10-571f-4298-9fea-2a108a207e7c"))
    case r@TriggeredMoveRequest("У реки", direction, "Пристань") if direction == "trigger_fortress_pereyaslavl" =>
      sender ! new SimpleCommand("дать 10 кун лодочник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Вы приплыли на Переяславльский берег и вылезли из лодки..*") =>
          person ! MoveEvent(
            persister.locationByTitle(r.from).headOption,
            direction,
            persister.locationByTitle(r.to).head)
          context.unbecome()
      }
    case r@TriggeredMoveRequest("Пристань", direction, "У реки") if direction == "trigger_pereyaslavl_fortress" =>
      sender ! new SimpleCommand("дать 10 кун лодочник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Вы приплыли к южному берегу реки..*") =>
          person ! MoveEvent(
            persister.locationByTitle(r.from).headOption,
            direction,
            persister.locationByTitle(r.to).head)
          context.unbecome()
      }
    case r@TriggeredMoveRequest("Пристань", direction, "Крутой берег") if direction == "trigger_pereyaslavl_fisherman_village" =>
      sender ! new SimpleCommand("дать 100 кун лодочник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Вы приплыли к рыбацкой деревне..*") =>
          person ! MoveEvent(
            persister.locationByTitle(r.from).headOption,
            direction,
            persister.locationByTitle(r.to).head)
          context.unbecome()
      }
    case r@TriggeredMoveRequest("Крутой берег", direction, "Пристань") if direction == "trigger_fisherman_village_pereyaslavl" =>
      sender ! new SimpleCommand("дать 100 кун лодочник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Вы приплыли к Переяславлю и вылезли из лодки..*") =>
          person ! MoveEvent(
            persister.locationByTitle(r.from).headOption,
            direction,
            persister.locationByTitle(r.to).head)
          context.unbecome()
      }
    case r@TriggeredMoveRequest(_, direction, _) if direction == "trigger_cursed_village_nk_trees" =>
      sender ! new SimpleCommand("лезть дерево")
      person ! MoveEvent(
        persister.loadLocation("2c9b5a58-c87a-4443-846c-fa3847d6d953").some,
        direction,
        persister.loadLocation("a4f34817-9335-4c45-8548-6a64f4b8fd95"))
    case r@TriggeredMoveRequest(_, direction, _) if direction == "trigger_nk_cursed_village_trees" =>
      sender ! new SimpleCommand("лезть дерево")
      person ! MoveEvent(
        persister.loadLocation("a4f34817-9335-4c45-8548-6a64f4b8fd95").some,
        direction,
        persister.loadLocation("2c9b5a58-c87a-4443-846c-fa3847d6d953"))
    case r@TriggeredMoveRequest("На полянке", direction, "Опушка в лесу") if direction == "trigger_cursed_village_nk_forester" =>
      sender ! new SimpleCommand(s"дать ${level} кун стар")
      person ! MoveEvent(
        persister.locationByTitle(r.from).headOption,
        direction,
        persister.locationByTitle(r.to).head)
    case r@TriggeredMoveRequest("Опушка в лесу", direction, "На полянке") if direction == "trigger_nk_forester_cursed_village" =>
      sender ! new SimpleCommand(s"дать ${level} кун стар")
      person ! MoveEvent(
        persister.locationByTitle(r.from).headOption,
        direction,
        persister.locationByTitle(r.to).head)
    case r@TriggeredMoveRequest("Медвежья берлога", direction, "Лесная поляна") if direction == "trigger_bearLair_deepForest" =>
      enterPentagram
      person ! MoveEvent(
        persister.locationByTitle(r.from).headOption,
        direction,
        persister.loadLocation("184d0886-7897-445a-ad1a-9874c0124d88"))
    case r@TriggeredMoveRequest("Берлога лешего", direction, "Медвежья берлога") if direction == "trigger_foresterLair_bearLair" =>
      enterPentagram
      person ! MoveEvent(
        persister.locationByTitle(r.from).headOption,
        direction,
        persister.locationByTitle(r.to).head)
  }

  private def enterPentagram {
    sender ! new SimpleCommand("приказ все войти пент")
    sender ! new SimpleCommand("войти пент")
  }
}
