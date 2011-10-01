package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.command.FillLiquidContainerCommand;
import com.tort.mudai.mapper.JMapperWrapper;
import com.tort.mudai.mapper.Location;

public class FillLiquidContainerTask extends StatedTask {
    private FillLiquidContainerCommand _command;
    private TravelTask _travelTask;
    private String _liquidContainer;
    private JMapperWrapper _mapper;
    private final TaskTerminateCallback _callback;

    @Inject
    public FillLiquidContainerTask(final EventDistributor eventDistributor,
                                   final TravelTaskFactory travelTaskFactory,
                                   final JMapperWrapper mapper,
                                   final PersonProperties personProperties,
                                   @Assisted TaskTerminateCallback callback) {
        _mapper = mapper;
        _callback = callback;
        _liquidContainer = personProperties.getLiquidContainer();

        final Location to = mapper.nearestWaterSource();
        if(to != null) {
            _travelTask = travelTaskFactory.create(to, new TravelTaskTerminateCallback());
            eventDistributor.subscribe(_travelTask);
        } else {
            System.out.println("NO WATER SOURCES");
            _callback.succeeded();
        }
    }

    @Override
    public RenderableCommand pulse() {
        if (_travelTask != null) {
            if (_travelTask.isTerminated()) {
                if (_travelTask.isFailed()) {
                    fail();
                    _callback.failed();
                    return null;
                }
                _command = new FillLiquidContainerCommand(_mapper.currentLocation().waterSource().get(), _liquidContainer);
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
