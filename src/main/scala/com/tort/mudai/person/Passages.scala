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
    case TriggeredMoveRequest("У шалаша", direction, "Тихий угол") =>
      sender ! new SimpleCommand(s"дать $level кун следопыт")
    case TriggeredMoveRequest("Тихий угол", direction, "У шалаша") =>
      sender ! new SimpleCommand(s"дать $level кун следопыт")
    case TriggeredMoveRequest("На лугу", direction, "На лугу") if direction == "v1_v2_trigger" =>
      sender ! new SimpleCommand(s"дать $level кун цыган")
      person ! MoveEvent(persister.loadLocation("a2487caf-444f-4736-978f-0f1fbbd6083d").some,
        direction.some,
        persister.loadLocation("ebb1d973-693f-4d3c-95f9-8b0f187f7eaa"))
    case TriggeredMoveRequest("На лугу", direction, "На лугу") if direction == "v2_v1_trigger" =>
      sender ! new SimpleCommand(s"дать $level кун цыган")
      person ! MoveEvent(persister.loadLocation("ebb1d973-693f-4d3c-95f9-8b0f187f7eaa").some,
        direction.some,
        persister.loadLocation("a2487caf-444f-4736-978f-0f1fbbd6083d"))
    case TriggeredMoveRequest("Дорога", direction, "Дорога") if direction == "trigger_swamp_south" =>
      sender ! new SimpleCommand(s"дать ${level * 2} кун болотник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Но наконец путь кончился, и Вы уже за болотами..*") =>
          person ! MoveEvent(persister.loadLocation("f6c1f708-c24c-41cc-b653-9ff36a64e731").some,
            direction.some,
            persister.loadLocation("9e0df04f-4bf2-4313-b305-fa84d574300b"))
          context.unbecome()
      }
    case TriggeredMoveRequest("Дорога", direction, "Дорога") if direction == "trigger_swamp_north" =>
      sender ! new SimpleCommand(s"дать ${level} кун болотник")
      person ! MoveEvent(persister.loadLocation("2b7f6585-69eb-4e9f-8d46-bb649b42ca36").some,
        direction.some,
        persister.loadLocation("fcb3a6b8-0393-423e-aae6-7c2335e2c3bc"))
    case TriggeredMoveRequest("У ямы", direction, "У ямы") if direction == "trigger_jump_north" =>
      sender ! new SimpleCommand("перепрыгнуть яма")
      person ! MoveEvent(persister.loadLocation("b29f1fc5-01f9-42ca-a7ca-662e11e8866f").some,
        direction.some,
        persister.loadLocation("a2fb7b2f-4830-4900-94f2-9e6dea318daf"))
    case TriggeredMoveRequest("У ямы", direction, "У ямы") if direction == "trigger_jump_south" =>
      sender ! new SimpleCommand("перепрыгнуть яма")
      person ! MoveEvent(persister.loadLocation("a2fb7b2f-4830-4900-94f2-9e6dea318daf").some,
        direction.some,
        persister.loadLocation("b29f1fc5-01f9-42ca-a7ca-662e11e8866f"))
    case r@TriggeredMoveRequest("Сокровищница", direction, "Широкий проход") if direction == "trigger_treasure_down" =>
      sender ! new SimpleCommand("г сезам откройся")
      person ! MoveEvent(persister.locationByTitle(r.from).headOption,
        direction.some,
        persister.loadLocation("198a99ad-0e32-49c6-a189-7378c1b774ae"))
    case r@TriggeredMoveRequest("У реки", direction, "Пристань") if direction == "trigger_fortress_pereyaslavl" =>
      sender ! new SimpleCommand("дать 10 кун лодочник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Вы приплыли на Переяславльский берег и вылезли из лодки..*") =>
          person ! MoveEvent(persister.locationByTitle(r.from).headOption,
            direction.some,
            persister.locationByTitle(r.to).head)
          context.unbecome()
      }
    case r@TriggeredMoveRequest("Пристань", direction, "У реки") if direction == "trigger_pereyaslavl_fortress" =>
      sender ! new SimpleCommand("дать 10 кун лодочник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Вы приплыли к южному берегу реки..*") =>
          person ! MoveEvent(persister.locationByTitle(r.from).headOption,
            direction.some,
            persister.locationByTitle(r.to).head)
          context.unbecome()
      }
    case r@TriggeredMoveRequest("Пристань", direction, "Крутой берег") if direction == "trigger_pereyaslavl_fisherman_village" =>
      sender ! new SimpleCommand("дать 100 кун лодочник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Вы приплыли к рыбацкой деревне..*") =>
          person ! MoveEvent(persister.locationByTitle(r.from).headOption,
            direction.some,
            persister.locationByTitle(r.to).head)
          context.unbecome()
      }
    case r@TriggeredMoveRequest("Крутой берег", direction, "Пристань") if direction == "trigger_fisherman_village_pereyaslavl" =>
      sender ! new SimpleCommand("дать 100 кун лодочник")
      context.become {
        case RawRead(text) if text.matches("(?ms).*Вы приплыли к Переяславлю и вылезли из лодки..*") =>
          person ! MoveEvent(persister.locationByTitle(r.from).headOption,
            direction.some,
            persister.locationByTitle(r.to).head)
          context.unbecome()
      }
  }
}
