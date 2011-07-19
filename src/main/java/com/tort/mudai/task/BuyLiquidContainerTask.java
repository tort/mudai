package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.BuyCommand;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Location;
import com.tort.mudai.mapper.Mapper;
import com.tort.mudai.mapper.MapperException;

public class BuyLiquidContainerTask extends StatedTask {
    private GoAndDoTask _goAndDoTask;
    private final TaskTerminateCallback _callback;

    @Inject
    public BuyLiquidContainerTask(final Mapper mapper,
                                  final PersonProperties personProperties,
                                  final GoAndDoTaskFactory goAndDoTaskFactory,
                                  @Assisted TaskTerminateCallback callback) {
        _callback = callback;
        try {
            Location to = mapper.nearestShop();
            _goAndDoTask = goAndDoTaskFactory.create(to, new BuyCommand(personProperties.getLiquidContainer()));
        } catch (MapperException e) {
            //TODO replace with some logging
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Override
    public Command pulse() {
        if (_goAndDoTask.isTerminated()) {
            if (_goAndDoTask.isSucceeded()) {
                _callback.succeeded();
            } else {
                _callback.failed();
            }
        }

        if (isTerminated())
            return null;

        return _goAndDoTask.pulse();
    }
}
