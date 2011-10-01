package com.tort.mudai.task

import actors.Actor
import Actor._
import com.tort.mudai.mapper.{Direction, Mapper, Location}
import java.lang.String
import com.tort.mudai.event.{DiscoverObstacleEvent, GlanceEvent}
import com.tort.mudai.command._
import com.tort.mudai.{SimpleMudClient, RoomSnapshot}

class TravelActor(val location: Location,
                  val mapper: Mapper) extends StatedTask {

  val NoopCommand = null
  val DefaultObstacle = "дверь"

  val actor = Actor.actor(act())

  override def glance(roomSnapshot: RoomSnapshot) {
    actor ! GlanceEvent(roomSnapshot, None)
  }

  override def glance(direction: String, roomSnapshot: RoomSnapshot) {
    actor ! GlanceEvent(roomSnapshot, Option(direction))
  }

  override def pulse() = {
    println(actor.getState)
    if (actor.getState != State.Terminated) {
      actor !? GetCommand() match {
        case CommandEvent(command) => command
        case _ => null
      }
    } else
      null
  }

  override def discoverObstacle(obstacle: String) {
    actor ! DiscoverObstacleEvent(obstacle)
  }

  def checkStepExpected(path: scala.Seq[Direction], direction: String) {
    val expectedDirection = path.head.getName
    if (direction != expectedDirection) {
      println("path lost, expected direction " + expectedDirection + ", but found " + direction)
      fail()
      exit
    }
  }

  private def waitTargetReached(path: Seq[Direction], commands: Seq[RenderableCommand]) {

    react {
      case GlanceEvent(roomSnapshot, Some(direction)) =>
        checkStepExpected(path, direction)
        if (mapper.currentLocation == location) {
          succeed()
        } else {
          val newPath = path.drop(1)
          waitTargetReached(newPath, commands :+ new MoveCommand(newPath.head))
        }
      case GetCommand() =>
        val command = commands.headOption.getOrElse(NoopCommand)
        sender ! CommandEvent(command)
        waitTargetReached(path, commands.drop(1))
      case DiscoverObstacleEvent(obstacle) =>
        val openAndMove = Seq(new OpenCommand(path.head, Option(obstacle).getOrElse(DefaultObstacle)), new MoveCommand(path.head))
        waitTargetReached(path, commands ++ openAndMove)
    }
  }

  private def act() {
    val path = mapper.pathTo(location)
    if (path.isEmpty)
      succeed()
    else
      waitTargetReached(path, Seq(new MoveCommand(path.head)))
  }

  case class GetCommand()

  case class CommandEvent(command: RenderableCommand)
}

object TravelActor {
  def apply(to: Location) = {
    val mapper = SimpleMudClient.injector.getInstance(classOf[Mapper])
    new TravelActor(to, mapper)
  }
}
