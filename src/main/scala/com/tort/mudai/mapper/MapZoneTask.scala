package com.tort.mudai.mapper

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import com.tort.mudai.{SimpleMudClient, PulseDistributor, RoomSnapshot}
import com.tort.mudai.task._
import scala.actors.Actor._
import com.tort.mudai.command._
import com.tort.mudai.event.{TakeItemEvent, DiscoverObstacleEvent, GlanceEvent}

class MapZoneTask @Inject()(@Assisted val zoneName: String,
                            val mapper: MapperImpl,
                            val eventDistributor: EventDistributor,
                            val pulseDistributor: PulseDistributor,
                            val directionHelper: DirectionHelper,
                            val persister: Persister) extends StatedTask with TravelHelper with PulseHelper with LocationHelper {
  val zone = new Zone(zoneName)

  override def glance(roomSnapshot: RoomSnapshot) {
    taskActor ! GlanceEvent(roomSnapshot, None)
  }

  override def glance(direction: String, roomSnapshot: RoomSnapshot) {
    taskActor ! GlanceEvent(roomSnapshot, Some(direction))
  }

  override def discoverObstacle(obstacle: String) {
    taskActor ! DiscoverObstacleEvent(obstacle)
  }

  override def pulse = handlePulse(taskActor)


  override def takeItem(item: String) {
    taskActor ! TakeItemEvent(item)
  }

  val taskActor = actor({
    memAndVisit(None, Set())
    println("### FINISHED MAP ZONE ACTOR ###")
  })

  private def goAndLook(target: Target, mapped: Set[Target]): Set[Target] = {
    travelTo(mapper, target.location)
    move(target)
    memAndVisit(Some(target), mapped + target + Target(mapper.currentLocation, directionHelper.getOppositeDirection(target.direction)))
  }

  private def memAndVisit(from: Option[Target], mapped: Set[Target]): Set[Target] = {
    memTargets(from, mapped).foldLeft(mapped)((acc, target) => goAndLook(target, acc) ++ acc)
  }

  def dropCoin() {
    receive {
      case _: GetCommand =>
        sender ! new CommandEvent(new DropCoinsCommand(1))
    }
  }

  def back(direction: Direction) {
    receive {
      case _: GetCommand =>
        sender ! new CommandEvent(new MoveCommand(direction))
        receive {
          case _: GlanceEvent =>
        }
    }
  }

  private def findMarkedRoomBetweenSimilar(locations: Seq[Location], target: Target): Boolean = {
    locations.map {
      loc =>
        travelTo(mapper, loc).map(snapshot =>
          snapshot.objectsPresent.find(obj => 
            obj.startsWith("Одна куна лежит здесь.")
          ).map(x => {mapper.mapExits(target.location, target.direction.getName, mapper.currentLocation); true}).getOrElse(false)
        ).getOrElse(false)
    }.reduce((acc, item) => acc || item)
  }

  private def waitForMove(target: Target, commands: Seq[RenderableCommand]) {
    receive {
      case DiscoverObstacleEvent(obstacle) => waitForMove(target, Seq(new OpenCommand(new Direction(target.direction.getName), obstacle), new MoveCommand(new Direction(target.direction.getName))))
      case GlanceEvent(roomSnapshot, Some(direction)) =>
        if (mapper.isPaused) {
          dropCoin()
          back(new Direction(directionHelper.getOppositeDirection(target.direction).getName))
          mapper.current(target.location)
          val locations = persister.loadLocations(createLocation(roomSnapshot))
          if (!findMarkedRoomBetweenSimilar(locations, target)) {
            val newLocation = mapper.mapNewLocation(roomSnapshot)
            mapper.mapExits(target.location, target.direction.getName, newLocation.get)
            println("MAPPED NEW")
          }
          travelTo(mapper, target.location)
          move(target)
          takeCoin()
        }
      case _: GetCommand =>
        val command = commands.headOption.orNull
        reply(CommandEvent(command))
        waitForMove(target, commands.drop(1))
    }
  }

  private def takeCoin() {
    receive {
      case _: GetCommand =>
        reply(CommandEvent(new SimpleCommand("взять кун")))
      //        receive {
      //          case TakeItemEvent(item) => println("TAKEN " + item)
      //        }
    }
  }

  private def move(target: Target) {
    receive {
      case _: GetCommand =>
        sender ! new CommandEvent(new MoveCommand(new Direction(target.direction.getName))) //TODO shit. get rid of this
        waitForMove(target, Seq())
    }
  }

  private def memTargets(from: Option[Target], mapped: Set[Target]) = {
    mapper.currentLocation.zone = Some(zone)

    val dirs = mapper.currentLocation.exits
    val targetsToMap = dirs.filterNot(_.border()).filterNot(d => from.map(directionHelper.getOppositeDirection(d) == _.direction).getOrElse(false)).map(Target(mapper.currentLocation, _)).filterNot(mapped.contains(_))
    targetsToMap
  }

  case class Target(location: Location, direction: Directions)

}

object MapZoneTask {
  val injector = SimpleMudClient.injector
  val mapper = injector.getInstance(classOf[MapperImpl])
  val eventDistributor = injector.getInstance(classOf[EventDistributor])
  val pulseDistributor = injector.getInstance(classOf[PulseDistributor])
  val directionHelper = injector.getInstance(classOf[DirectionHelper])
  val persister = injector.getInstance(classOf[Persister])

  def apply(zoneName: String) = new MapZoneTask(zoneName, mapper, eventDistributor, pulseDistributor, directionHelper, persister)
}

