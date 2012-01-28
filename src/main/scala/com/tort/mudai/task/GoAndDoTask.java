package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.mapper.Location;

public class GoAndDoTask extends StatedTask {
    private RenderableCommand _command;
    private AbstractTask _travelTask;
    private final RenderableCommand _afterTravelCommand;
    private final TaskTerminateCallback _callback;

    @Inject
    public GoAndDoTask(final EventDistributor eventDistributor,
                                   @Assisted final Location to,
                                   @Assisted final RenderableCommand command,
                                   @Assisted final TaskTerminateCallback callback) {
        _afterTravelCommand = command;
        _callback = callback;

        _travelTask = TravelActor.apply(to);
        eventDistributor.subscribe(_travelTask);
    }

    @Override
    public RenderableCommand pulse() {
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
        RenderableCommand command = _command;
        _command = null;

        return command;
    }
}
