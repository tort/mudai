package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Location;

public class GoAndDoTask extends StatedTask {
    private Command _command;
    private TravelTask _travelTask;
    private final Command _afterTravelCommand;
    private final TaskTerminateCallback _callback;

    @Inject
    public GoAndDoTask(final EventDistributor eventDistributor,
                                   final TravelTaskFactory travelTaskFactory,
                                   @Assisted final Location to,
                                   @Assisted final Command command,
                                   @Assisted final TaskTerminateCallback callback) {
        _afterTravelCommand = command;
        _callback = callback;

        _travelTask = travelTaskFactory.create(to, new TravelTaskTerminateCallback());
        eventDistributor.subscribe(_travelTask);
    }

    @Override
    public Command pulse() {
        // TODO refactor to explicit states
        if (_travelTask != null) {
            if (_travelTask.isTerminated()) {
                if (_travelTask.isFailed()) {
                    fail();
                    _callback.failed();
                    return null;
                }
                _command = _afterTravelCommand;
                _travelTask = null;
                succeed();
                _callback.succeeded();
            } else {
                return _travelTask.pulse();
            }
        }
        Command command = _command;
        _command = null;

        return command;
    }

    private class TravelTaskTerminateCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {

        }

        @Override
        public void failed() {
            fail();
        }
    }
}
