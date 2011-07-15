package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Location;

public class GoAndDoTask extends StatedTask {
    private Command _command;
    private TravelTask _travelTask;
    private final Command _afterTravelCommand;

    @Inject
    public GoAndDoTask(final EventDistributor eventDistributor,
                                   final TravelTaskFactory travelTaskFactory,
                                   @Assisted final Location to,
                                   @Assisted final Command command) {
        _afterTravelCommand = command;

        _travelTask = travelTaskFactory.create(to);
        eventDistributor.subscribe(_travelTask);
    }

    @Override
    public Command pulse() {
        // TODO refactor to explicit states
        if (_travelTask != null) {
            if (_travelTask.isTerminated()) {
                if (_travelTask.isFailed()) {
                    fail();
                    return null;
                }
                _command = _afterTravelCommand;
                _travelTask = null;
                succeed();
            } else {
                return _travelTask.pulse();
            }
        }
        Command command = _command;
        _command = null;

        return command;
    }

}
