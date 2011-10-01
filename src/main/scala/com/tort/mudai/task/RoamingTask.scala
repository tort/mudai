package com.tort.mudai.task

import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import com.tort.mudai.command.RenderableCommand
import com.tort.mudai.mapper.{Location, Mapper, Persister}

class RoamingTask @Inject()(val persister: Persister,
                            val travelTaskFactory: TravelTaskFactory,
                            val eventDispatcher: EventDistributor,
                            val mapper: Mapper) extends StatedTask {

    var _beforeRoamLocation: Location = _
    var _locations: Seq[Location] = persister.enlistMobs.flatMap(mob => mob.habitationArea)
    var _travelTask: TravelTask = _
    var _finishRoamTask: TravelTask = _
    var _killTask: KillTask = _

    override def glance(roomSnapshot: RoomSnapshot) {
        killMob(roomSnapshot)
        removeVisitedFromRoamPath()
    }

    private def removeVisitedFromRoamPath() {
      _locations = _locations.filterNot(mapper.currentLocation.equals(_))
    }

    private def killMob(roomSnapshot: RoomSnapshot) {
        for (mobName <- roomSnapshot.mobs) {
            val mob = persister.findMob(mobName)
            if (mob != null) {
                _killTask = new KillTask(mob, new TaskTerminateCallback() {
                    override def succeeded() {
                        _killTask = null
                    }

                    override def failed() {
                        fail()
                    }
                })
                eventDispatcher.subscribe(_killTask)

                return
            }
        }
    }

    override def pulse: RenderableCommand = {
        if (isTerminated())
            return null

        if (_killTask != null) {
            return _killTask.pulse()
        }

        if (_travelTask == null && _finishRoamTask == null) {
            _beforeRoamLocation = mapper.currentLocation
            val to = _locations.head
            if (to != null) {
                _travelTask = travelTaskFactory.create(to, new TravelTerminateCallback())
                eventDispatcher.subscribe(_travelTask);
            } else {
                succeed();
                System.out.println("NO LOCATIONS TO ROAM");
            }
        }

        var command: RenderableCommand = null
        if (_travelTask != null)
            command = _travelTask.pulse()

        if (_finishRoamTask != null)
            command = _finishRoamTask.pulse()

        return command
    }

    private class TravelTerminateCallback extends TaskTerminateCallback {
        override def succeeded {
            val to = _locations.head
            if (to != null) {
                _travelTask = travelTaskFactory.create(to, new TravelTerminateCallback())
                eventDispatcher.subscribe(_travelTask)
            } else {
                _travelTask = null
                _finishRoamTask = travelTaskFactory.create(_beforeRoamLocation, new ReturnHomeTerminateCallback())
                eventDispatcher.subscribe(_finishRoamTask)
            }
        }

        override def failed {
            fail()
            System.out.println("FAIL ROAM")
        }

        private class ReturnHomeTerminateCallback extends TaskTerminateCallback {
            override def succeeded {
                succeed()
            }

            override def failed {
                fail()
            }
        }
    }
}
