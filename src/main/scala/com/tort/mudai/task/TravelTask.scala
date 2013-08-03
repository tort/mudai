package com.tort.mudai.task

import actors.Actor
import Actor._
import com.tort.mudai.mapper.{Direction, Mapper, Location}
import java.lang.String
import com.tort.mudai.event.{DiscoverObstacleEvent, GlanceEvent}
import com.tort.mudai.command._
import com.tort.mudai.{SimpleMudClient, RoomSnapshot}

class TravelTask(val location: Location,
                  val mapper: Mapper) extends StatedTask with TravelHelper with PulseHelper {

  val actor = Actor.actor({
    travelTo(mapper, location)
    succeed()
  })

  override def glance(roomSnapshot: RoomSnapshot) {
    actor ! GlanceEvent(roomSnapshot, None)
  }

  override def glance(direction: Direction, roomSnapshot: RoomSnapshot) {
    actor ! GlanceEvent(roomSnapshot, Option(direction))
  }

  override def pulse() = handlePulse(actor)

  override def discoverObstacle(obstacle: String) {
    actor ! DiscoverObstacleEvent(obstacle)
  }
}

trait PulseHelper {
  def handlePulse(actor: Actor): RenderableCommand = {
    if (actor.getState != State.Terminated) {
      actor !? GetCommand() match {
        case CommandEvent(command) => command
        case _ => null
      }
    } else
      null
  }
}

object TravelTask {
  def apply(to: Location) = {
    val mapper = SimpleMudClient.injector.getInstance(classOf[Mapper])
    new TravelTask(to, mapper)
  }
}

trait TravelHelper {
  val NoopCommand = null
  val DefaultObstacle = "дверь"

  private def waitTargetReached(targetLocation: Location, path: Seq[Direction], commands: Seq[RenderableCommand], mapper: Mapper): RoomSnapshot = {
    receive {
      case GlanceEvent(roomSnapshot, Some(direction)) =>
        checkStepExpected(path, direction)
            if (mapper.currentLocation == targetLocation) {
              roomSnapshot
            } else {
              val newPath = path.drop(1)
              waitTargetReached(targetLocation, newPath, commands :+ new MoveCommand(newPath.head), mapper)
            }
      case GetCommand() =>
        val command = commands.headOption.getOrElse(NoopCommand)
        sender ! CommandEvent(command)
        waitTargetReached(targetLocation, path, commands.drop(1), mapper)
      case DiscoverObstacleEvent(obstacle) =>
        val openAndMove = Seq(new OpenCommand(path.head, Option(obstacle).getOrElse(DefaultObstacle)), new MoveCommand(path.head))
        waitTargetReached(targetLocation, path, commands ++ openAndMove, mapper)
    }
  }

  private def checkStepExpected(path: scala.Seq[Direction], direction: Direction) {
    val expectedDirection = path.head.id
    if (direction != expectedDirection) {
      println("path lost, expected direction " + expectedDirection + ", but found " + direction)
      exit()
    }
  }

  def travelTo(mapper: Mapper, targetLocation: Location): Option[RoomSnapshot] = {
    val path = mapper.pathTo(targetLocation)
    if (path.isEmpty){
      None
    } else
      Some(waitTargetReached(targetLocation, path, Seq(new MoveCommand(path.head)), mapper))
  }

  case class LocationEvent(location: Location)

  trait Result
  case class Success() extends Result
  case class Failure() extends Result

  type Callback = (Result) => Unit
}

case class GetCommand()
case class CommandEvent(command: RenderableCommand)

