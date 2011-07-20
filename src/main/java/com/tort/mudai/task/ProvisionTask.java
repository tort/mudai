package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProvisionTask extends StatedTask {
    private static final Command EMPTY_COMMAND = null;

    private final EventDistributor _eventDistributor;
    private final List<Task> _subtasks = new CopyOnWriteArrayList<Task>();

    private final EatTaskFactory _eatTaskFactory;
    private final DrinkTaskFactory _drinkTaskFactory;

    @Inject
    public ProvisionTask(final EventDistributor eventDistributor,
                         final DrinkTaskFactory drinkTaskFactory,
                         final EatTaskFactory eatTaskFactory) {
        _eventDistributor = eventDistributor;
        _drinkTaskFactory = drinkTaskFactory;
        _eatTaskFactory = eatTaskFactory;

        final EatTask eatTask = _eatTaskFactory.create(new EatTaskCallback());
        _subtasks.add(eatTask);
        _eventDistributor.subscribe(eatTask);

        final DrinkTask drinkTask = _drinkTaskFactory.create(new DrinkTaskCallback());
        _subtasks.add(drinkTask);
        _eventDistributor.subscribe(drinkTask);
    }

    @Override
    public Command pulse() {
        for (Task task : _subtasks) {
            Command command = task.pulse();
            if (command != EMPTY_COMMAND) {
                return command;
            }

            if (task.isInitializing())
                break;
        }

        List<Task> toDelete = new ArrayList();
        for (Task subtask : _subtasks) {
            if (subtask.isTerminated())
                toDelete.add(subtask);
        }

        for (Task task : toDelete) {
            _subtasks.remove(task);
        }

        return null;
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
