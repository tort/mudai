package com.tort.mudai.task

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.name.Named
import com.tort.mudai.CommandExecutor
import com.tort.mudai.PulseDistributor
import scala.collection.JavaConversions._

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import com.tort.mudai.mapper.{Mapper, Location, MapZoneTaskFactory}

class Person @Inject()(val _sessionProvider: Provider[SessionTask],
                       @Named("mapperTask") val _mapperTaskProvider: Provider[AbstractTask],
                       val _provisionTaskProvider: Provider[ProvisionTask],
                       val _commandExecutor: CommandExecutor,
                       val _pulseExecutor: ScheduledExecutorService,
                       val _eventDistributor: EventDistributor,
                       val _pulseDistributor: PulseDistributor,
                       val _roamingTaskProvider: Provider[RoamingTask],
                       val _mapZoneTaskFactory: MapZoneTaskFactory,
                       val mapper: Mapper
                       ) extends StatedTask {

  run()

  def subscribe(task: AbstractTask) {
    _eventDistributor.subscribe(task)
  }

  def start() {
    val sessionTask = _sessionProvider.get()
    val mapperTask = _mapperTaskProvider.get()

    _eventDistributor.subscribe(sessionTask)
    _eventDistributor.subscribe(mapperTask)

    _pulseDistributor.subscribe(sessionTask)
    _pulseDistributor.subscribe(mapperTask)

    _pulseExecutor.scheduleAtFixedRate(new Runnable() {
      override def run() {
        try {
          val p = pulse()
          if (p != null) {
            _commandExecutor.submit(p)
          }
        } catch {
          case e: Exception => e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
        }
      }
    }, 1, 1, TimeUnit.SECONDS)
  }

  def travel(to: Location) {
    val travelTask = TravelActor(to)
    _eventDistributor.subscribe(travelTask)
    _pulseDistributor.subscribe(travelTask)
  }

  def roam() {
    val roamingTask = _roamingTaskProvider.get()
    _eventDistributor.subscribe(roamingTask)
    _pulseDistributor.subscribe(roamingTask)
  }

  override def pulse() = {
    val command = _pulseDistributor.pulse()
    for (task <- _eventDistributor.getTargets) {
      if (task.isTerminated) {
        _eventDistributor.unsubscribe(task)
      }
    }

    command
  }

  def provision() {
    val provisionTask = _provisionTaskProvider.get()
    _eventDistributor.subscribe(provisionTask)
    _pulseDistributor.subscribe(provisionTask)
  }

  def mapZone(zoneName: String) {
    val mapZoneTask = _mapZoneTaskFactory.create(zoneName)
    _eventDistributor.subscribe(mapZoneTask)
    _pulseDistributor.subscribe(mapZoneTask)
  }
}
