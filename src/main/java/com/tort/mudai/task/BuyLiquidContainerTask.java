package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.BuyCommand;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Location;
import com.tort.mudai.mapper.Mapper;
import com.tort.mudai.mapper.MapperException;

public class BuyLiquidContainerTask extends StatedTask {
    private BuyCommand _command;
    private TravelTask _travelTask;
    private String _liquidContainer;

    @Inject
    public BuyLiquidContainerTask(final EventDistributor eventDistributor,
                                  final TravelTaskFactory travelTaskFactory,
                                  final Mapper mapper,
                                  final PersonProperties personProperties) {
        _liquidContainer = personProperties.getLiquidContainer();

        try {
            final Location to = mapper.nearestWaterSource();
            _travelTask = travelTaskFactory.create(to);
            eventDistributor.subscribe(_travelTask);
        } catch (MapperException e) {
            System.out.println("NO WATER SOURCES");
            succeed();
        }
    }

    @Override
    public Command pulse() {
        if (_travelTask != null) {
            if (_travelTask.isTerminated()) {
                if (_travelTask.isFailed()) {
                    fail();
                    return null;
                }
                _command = new BuyCommand(_liquidContainer);
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
