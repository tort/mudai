package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.FillLiquidContainerCommand;
import com.tort.mudai.mapper.Location;
import com.tort.mudai.mapper.Mapper;
import com.tort.mudai.mapper.MapperException;

public class FillLiquidContainerTask extends StatedTask {
    private FillLiquidContainerCommand _command;
    private TravelTask _travelTask;
    private String _liquidContainer;
    private Mapper _mapper;
    private final TaskTerminateCallback _callback;

    @Inject
    public FillLiquidContainerTask(final EventDistributor eventDistributor,
                                   final TravelTaskFactory travelTaskFactory,
                                   final Mapper mapper,
                                   final PersonProperties personProperties,
                                   @Assisted TaskTerminateCallback callback) {
        _mapper = mapper;
        _callback = callback;
        _liquidContainer = personProperties.getLiquidContainer();

        try {
            final Location to = mapper.nearestWaterSource();
            _travelTask = travelTaskFactory.create(to);
            eventDistributor.subscribe(_travelTask);
        } catch (MapperException e) {
            System.out.println("NO WATER SOURCES");
            _callback.succeeded();
        }
    }

    @Override
    public Command pulse() {
        if (_travelTask != null) {
            if (_travelTask.isTerminated()) {
                if (_travelTask.isFailed()) {
                    _callback.failed();
                    return null;
                }
                _command = new FillLiquidContainerCommand(_mapper.currentLocation().getWaterSource(), _liquidContainer);
                _travelTask = null;
                _callback.succeeded();
            } else {
                return _travelTask.pulse();
            }
        }
        Command command = _command;
        _command = null;

        return command;
    }

}
