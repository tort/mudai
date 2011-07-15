package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.BuyCommand;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Mapper;

public class BuyLiquidContainerTask extends StatedTask {
    private final GoAndDoTask _goAndDoTask;

    @Inject
    public BuyLiquidContainerTask(final Mapper mapper,
                                  final PersonProperties personProperties,
                                  final GoAndDoTaskFactory goAndDoTaskFactory) {
        _goAndDoTask = goAndDoTaskFactory.create(mapper.nearestShop(), new BuyCommand(personProperties.getLiquidContainer()));
    }

    @Override
    public Command pulse() {
        if (_goAndDoTask != null) {
            return _goAndDoTask.pulse();
        }

        return null;
    }
}
