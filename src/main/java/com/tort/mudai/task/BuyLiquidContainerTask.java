package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.command.BuyCommand;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Mapper;
import com.tort.mudai.mapper.MapperException;

public class BuyLiquidContainerTask extends StatedTask {
    private final EventDistributor _eventDistributor;
    private final TravelTaskFactory _travelTaskFactory;
    private final Mapper _mapper;
    private BuyCommand _command;
    private TravelTask _travelTask;

    @Inject
    public BuyLiquidContainerTask(final EventDistributor eventDistributor,
                                  final TravelTaskFactory travelTaskFactory,
                                  final Mapper mapper) {
        _eventDistributor = eventDistributor;
        _travelTaskFactory = travelTaskFactory;
        _mapper = mapper;

        try {
            final String to = _mapper.nearestWaterSource();
            _travelTask = _travelTaskFactory.create(to);
            _eventDistributor.subscribe(_travelTask);
        } catch (MapperException e) {
            System.out.println("NO WATER SOURCES");
            terminate();
        }
    }

    @Override
    public Command pulse() {
        if (_travelTask != null) {
            if (_travelTask.isTerminated()) {
                //TODO remobe hardcoded container name
                _command = new BuyCommand("фляг");
                _travelTask = null;
            } else {
                return _travelTask.pulse();
            }
        }
        Command command = _command;
        clearCommand();

        return command;
    }

    private void clearCommand() {
        _command = null;
    }
}
