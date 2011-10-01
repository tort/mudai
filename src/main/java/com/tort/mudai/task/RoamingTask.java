package com.tort.mudai.task;

import com.db4o.ObjectSet;
import com.google.inject.Inject;
import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Location;
import com.tort.mudai.mapper.Mapper;
import com.tort.mudai.mapper.Mob;
import com.tort.mudai.mapper.Persister;

import java.util.*;

public class RoamingTask extends StatedTask {
    private final Persister _persister;
    private final TravelTaskFactory _travelTaskFactory;
    private final EventDistributor _eventDispatcher;
    private final Mapper _mapper;

    private Location _beforeRoamLocation;
    private volatile Queue<Location> _locations;
    private volatile TravelTask _travelTask;
    private volatile TravelTask _finishRoamTask;
    private volatile KillTask _killTask;
    private Command _command;

    @Override
    public void lookAround(RoomSnapshot roomSnapshot) {
        killMob(roomSnapshot);
        removeVisitedFromRoamPath();
    }

    private void removeVisitedFromRoamPath() {
        for (Iterator<Location> iterator = _locations.iterator(); iterator.hasNext(); ) {
            Location location = iterator.next();
            if (_mapper.currentLocation().equals(location)) {
                iterator.remove();
            }
        }
    }

    private void killMob(final RoomSnapshot roomSnapshot) {
        for (String mobName : roomSnapshot.getMobs()) {
            final Mob mob = _persister.findMob(mobName);
            if (mob != null) {
                _killTask = new KillTask(mob, new TaskTerminateCallback() {
                    @Override
                    public void succeeded() {
                        _killTask = null;
                        _command = new SimpleCommand("смотр");
                    }

                    @Override
                    public void failed() {
                        fail();
                    }
                });
                _eventDispatcher.subscribe(_killTask);

                return;
            }
        }
    }

    @Inject
    public RoamingTask(Persister persister, TravelTaskFactory travelTaskFactory, EventDistributor eventDispatcher, Mapper mapper) {
        _persister = persister;
        _travelTaskFactory = travelTaskFactory;
        _eventDispatcher = eventDispatcher;
        _mapper = mapper;

        _locations = new LinkedList();

        final ObjectSet<Mob> mobs = persister.enlistMobs();
        Set<Location> locations = new HashSet();
        for (Mob mob : mobs) {
            locations.addAll(mob.habitationArea());
        }

        _locations.addAll(locations);
    }

    @Override
    public Command pulse() {
        if (isTerminated())
            return null;

        if(_command != null){
            Command command = _command;
            _command = null;
            return command;
        }

        if (_killTask != null) {
            return _killTask.pulse();
        }

        if (_travelTask == null && _finishRoamTask == null) {
            _beforeRoamLocation = _mapper.currentLocation();
            final Location to = _locations.poll();
            if (to != null) {
                _travelTask = _travelTaskFactory.create(to, new TravelTerminateCallback());
                _eventDispatcher.subscribe(_travelTask);
            } else {
                succeed();
                System.out.println("NO LOCATIONS TO ROAM");
            }
        }

        if (_travelTask != null)
            return _travelTask.pulse();

        if (_finishRoamTask != null)
            return _finishRoamTask.pulse();

        return null;
    }

    private class TravelTerminateCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {
            final Location to = _locations.poll();
            if (to != null) {
                _travelTask = _travelTaskFactory.create(to, new TravelTerminateCallback());
                _eventDispatcher.subscribe(_travelTask);
            } else {
                _travelTask = null;
                _finishRoamTask = _travelTaskFactory.create(_beforeRoamLocation, new ReturnHomeTerminateCallback());
                _eventDispatcher.subscribe(_finishRoamTask);
            }
        }

        @Override
        public void failed() {
            fail();
            System.out.println("FAIL ROAM");
        }

        private class ReturnHomeTerminateCallback implements TaskTerminateCallback {
            @Override
            public void succeeded() {
                succeed();
            }

            @Override
            public void failed() {
                fail();
            }
        }
    }
}
