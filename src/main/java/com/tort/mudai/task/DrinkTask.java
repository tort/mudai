package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.DrinkCommand;
import com.tort.mudai.event.LiquidContainer;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DrinkTask extends StatedTask {
    private final static int THIRST_INTERVAL = 12;
    private final static int HUNGER_INTERVAL = 21;

    private PersonProperties _personProperties;
    private volatile Command _command;
    private final DrinkCommand _drinkCommand;
    private ScheduledExecutorService _executor;

    @Inject
    public DrinkTask(final PersonProperties personProperties, final ScheduledExecutorService executor) {
        _personProperties = personProperties;
        _executor = executor;

        _drinkCommand = new DrinkCommand(_personProperties.getLiquidContainer());
        final Runnable drinkTask = new Runnable() {
            @Override
            public synchronized void run() {
                _command = _drinkCommand;
            }
        };
        _command = _drinkCommand;
        _executor.scheduleAtFixedRate(drinkTask, 0, THIRST_INTERVAL, TimeUnit.MINUTES);
        run();
    }

    @Override
    public Command pulse() {
        return _command;
    }

    @Override
    public void feelNotThirsty() {
        synchronized (this) {
            _command = null;
        }
    }

    @Override
    public void examineWaterContainer(final LiquidContainer.State state) {
        if(state == LiquidContainer.State.EMPTY){
            fail();
        }
    }
}
