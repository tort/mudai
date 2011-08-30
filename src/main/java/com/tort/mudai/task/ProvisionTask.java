package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.PulseDistributor;
import com.tort.mudai.command.Command;

public class ProvisionTask extends StatedTask {
    private final EventDistributor _eventDistributor;
    private final PulseDistributor _pulseDistributor;

    private final EatTaskFactory _eatTaskFactory;
    private final DrinkTaskFactory _drinkTaskFactory;

    @Inject
    public ProvisionTask(final EventDistributor eventDistributor,
                         final PulseDistributor pulseDistributor,
                         final DrinkTaskFactory drinkTaskFactory,
                         final EatTaskFactory eatTaskFactory) {
        _eventDistributor = eventDistributor;
        _drinkTaskFactory = drinkTaskFactory;
        _eatTaskFactory = eatTaskFactory;
        _pulseDistributor = pulseDistributor;

        final EatTask eatTask = _eatTaskFactory.create(new EatTaskCallback());
        _pulseDistributor.subscribe(eatTask);
        _eventDistributor.subscribe(eatTask);

        final DrinkTask drinkTask = _drinkTaskFactory.create(new DrinkTaskCallback());
        _pulseDistributor.subscribe(drinkTask);
        _eventDistributor.subscribe(drinkTask);
    }

    @Override
    public Command pulse() {
        if (isInitializing())
            run();

        return _pulseDistributor.pulse();
    }

    private class DrinkTaskCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {

        }

        @Override
        public void failed() {
            fail();
        }
    }

    private class EatTaskCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {

        }

        @Override
        public void failed() {
            fail();
        }
    }

}
