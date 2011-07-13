package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.DrinkCommand;
import com.tort.mudai.event.LiquidContainer;
import com.tort.mudai.mapper.Mapper;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DrinkTask extends StatedTask {
    private final static int THIRST_INTERVAL = 12;
    private final static int HUNGER_INTERVAL = 21;

    private PersonProperties _personProperties;
    private ScheduledExecutorService _executor;
    private final Mapper _mapper;
    private boolean _feelThirst = false;

    @Inject
    public DrinkTask(final PersonProperties personProperties,
                     final ScheduledExecutorService executor,
                     final Mapper mapper) {
        _personProperties = personProperties;
        _executor = executor;
        _mapper = mapper;

        final Runnable drinkTask = new Runnable() {
            @Override
            public synchronized void run() {
                _feelThirst = true;
            }
        };
        _executor.scheduleAtFixedRate(drinkTask, 0, THIRST_INTERVAL, TimeUnit.MINUTES);
        run();
    }

    @Override
    public Command pulse() {
        if(!_feelThirst)
            return null;

        final String waterSource = _mapper.currentLocation().getWaterSource();
        String whereFrom;
        if (waterSource == null) {
            whereFrom = _personProperties.getLiquidContainer();
        } else {
            whereFrom = waterSource;
        }
        return new DrinkCommand(whereFrom);
    }

    @Override
    public void feelNotThirsty() {
        synchronized (this) {
            _feelThirst = false;
        }
    }

    @Override
    public void examineWaterContainer(final LiquidContainer.State state) {
        if (state == LiquidContainer.State.EMPTY) {
            fail();
        }
    }
}
