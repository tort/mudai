package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.BuyCommand;
import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.mapper.JMapperWrapper;
import com.tort.mudai.mapper.Location;

public class BuyLiquidContainerTask extends StatedTask {
    private GoAndDoTask _goAndDoTask;
    private final TaskTerminateCallback _callback;

    @Inject
    public BuyLiquidContainerTask(final JMapperWrapper mapper,
                                  final PersonProperties personProperties,
                                  final GoAndDoTaskFactory goAndDoTaskFactory,
                                  @Assisted TaskTerminateCallback callback) {
        _callback = callback;
        Location to = mapper.nearestShop();
        if(to != null) {
            _goAndDoTask = goAndDoTaskFactory.create(to, new BuyCommand(personProperties.getLiquidContainer()), new TaskTerminateCallback(){
                @Override
                public void succeeded() {
                    succeed();
                    _callback.succeeded();
                }

                @Override
                public void failed() {
                    fail();
                    _callback.failed();
                }
            });
        } else {
            //TODO replace with some logging
            System.out.println("NO SHOPS EXIST");
            fail();
        }
    }

    @Override
    public RenderableCommand pulse() {
        if (isTerminated())
            return null;

        return _goAndDoTask.pulse();
    }
}
