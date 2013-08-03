package com.tort.mudai.task

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.name.Named
import com.tort.mudai.CommandExecutor
import com.tort.mudai.PulseDistributor

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import com.tort.mudai.mapper.{Mapper, Location}

class Person @Inject()(val sessionProvider: Provider[SessionTask],
                       @Named("mapperTask") val mapperTaskProvider: Provider[AbstractTask],
                       val commandExecutor: CommandExecutor,
                       val pulseExecutor: ScheduledExecutorService,
                       val eventDistributor: EventDistributor,
                       val pulseDistributor: PulseDistributor,
                       val mapper: Mapper
                       ) extends StatedTask {

  run()

  def subscribe(task: AbstractTask) {
    eventDistributor.subscribe(task)
  }

  def start() {
//    val sessionTask = sessionProvider.get()
    val mapperTask = mapperTaskProvider.get()

//    eventDistributor.subscribe(sessionTask)
    eventDistributor.subscribe(mapperTask)

//    pulseDistributor.subscribe(sessionTask)
    pulseDistributor.subscribe(mapperTask)

    pulseExecutor.scheduleAtFixedRate(new Runnable() {
      override def run() {
        try {
          val p = pulse()
          if (p != null) {
            commandExecutor.submit(p)
          }
        } catch {
          case e: Exception => e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
        }
      }
    }, 1, 1, TimeUnit.SECONDS)
  }

  def travel(to: Location) {
    val travelTask = TravelTask(to)
    eventDistributor.subscribe(travelTask)
    pulseDistributor.subscribe(travelTask)
  }

  override def pulse() = {
    val command = pulseDistributor.pulse()
    for (task <- eventDistributor.getTargets) {
      if (task.isTerminated) {
        eventDistributor.unsubscribe(task)
      }
    }

    command
  }
}

