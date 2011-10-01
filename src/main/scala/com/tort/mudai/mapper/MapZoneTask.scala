package com.tort.mudai.mapper

import com.google.inject.Inject
import collection.JavaConversions.JSetWrapper
import collection.mutable.HashSet
import com.google.inject.assistedinject.Assisted
import com.tort.mudai.{PulseDistributor, RoomSnapshot}
import com.tort.mudai.task.{EventDistributor, GoAndDoTaskFactory, TaskTerminateCallback, StatedTask}
import com.tort.mudai.command.SimpleCommand

class MapZoneTask @Inject()(@Assisted val zoneName: String,
                            val mapper: Mapper,
                            val goAndMapTaskFactory: GoAndMapTaskFactory,
                            val eventDistributor: EventDistributor,
                            val pulseDistributor: PulseDistributor,
                            val directionHelper: DirectionHelper) extends StatedTask {
  val zone = new Zone(zoneName)
  @volatile var unmappable = false
  @volatile var available: scala.collection.mutable.Set[(Location, Directions)] = new HashSet
  @volatile var mapped: scala.collection.mutable.Set[(Location, Directions)] = new HashSet

  memTargets()

  goAndLook()

  def goAndLook() {
    class GoAndMapTaskCallback(target: (Location, Directions)) extends TaskTerminateCallback {
      def failed() {
        fail()
      }

      def succeeded() {
        mapped += target
        if (unmappable) {
          unmappable = false
        } else {
          val optionLocation = target._1.getByDirection(target._2.getName).get
          val direction = directionHelper.getOppositeDirection(target._2)
          mapped += ((optionLocation, direction))
          memTargets()
        }
        goAndLook()
      }
    }

    if ((available -- mapped).isEmpty) {
      succeed()
    } else {
      val target = (available -- mapped).head
      val goAndMap = goAndMapTaskFactory.create(target._1, target._2, new GoAndMapTaskCallback(target))
      eventDistributor.subscribe(goAndMap)
      pulseDistributor.subscribe(goAndMap)
    }
  }


  def memTargets() {
    mapper.currentLocation.zone = Some(zone)

    val dirs = mapper.currentLocation.exits
    for (dir <- dirs) {
      if (!mapped.contains((mapper.currentLocation, dir)) && !dir.border()) {
        available += ((mapper.currentLocation, dir))
      }
    }
  }

  override def pulse = pulseDistributor.pulse()
}

trait MapZoneTaskFactory {
  def create(zoneName: String): MapZoneTask
}

trait GoAndMapTaskFactory {
  def create(location: Location, direction: Directions, callback: TaskTerminateCallback): GoAndMapTask
}

class GoAndMapTask @Inject()(@Assisted val to: Location,
                             @Assisted val direction: Directions,
                             @Assisted val callback: TaskTerminateCallback,
                             val goAndDoTaskFactory: GoAndDoTaskFactory,
                             val pulseDistributor: PulseDistributor,
                             val eventDistributor: EventDistributor) extends StatedTask {
  Console.println("WALKING " + to.title + " " + direction.getName())
  @volatile var awaitMove = false
  @volatile var awaitLook = false
  val goAndDo = goAndDoTaskFactory.create(to, new SimpleCommand(direction.getName()), new GoAndDoTaskCallback())
  eventDistributor.subscribe(goAndDo)
  pulseDistributor.subscribe(goAndDo)

  class GoAndDoTaskCallback extends TaskTerminateCallback {
    def succeeded() {
      Console.println("GoAndDo succeeded")
      awaitMove = true
    }

    def failed() {
      fail()
    }
  }

  override def glance(direction: String, roomSnapshot: RoomSnapshot) {
    if (awaitMove) {
      awaitLook = true
      awaitMove = false
    }
  }

  override def glance(locationTitle: RoomSnapshot) {
    if (awaitLook) {
      Console.println("GoAndMap succeeded")
      awaitLook = false
      succeed()
      callback.succeeded()
    }
  }

  override def pulse() = Option(pulseDistributor.pulse()).getOrElse(if (awaitLook) new SimpleCommand("смотр") else null)
}

