package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.ExamineItemCommand;
import com.tort.mudai.command.InventoryCommand;
import com.tort.mudai.event.LiquidContainer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProvisionTask extends StatedTask {
    private static final Command EMPTY_COMMAND = null;

    private final EventDistributor _eventDistributor;
    private final BuyLiquidContainerTaskFactory _buyLiquidContainerTaskFactory;
    private final FillLiquidContainerTaskFactory _fillLiquidContainerTaskProvider;
    private final DrinkTaskFactory _drinkTaskProvider;
    private final String _waterContainer;
    private final List<Task> _subtasks = new CopyOnWriteArrayList<Task>();

    private volatile Command _command;

    @Inject
    public ProvisionTask(final EventDistributor eventDistributor,
                         BuyLiquidContainerTaskFactory buyLiquidContainerTaskFactory,
                         final FillLiquidContainerTaskFactory fillLiquidContainerTaskProvider,
                         final DrinkTaskFactory drinkTaskProvider,
                         final PersonProperties personProperties) {
        _eventDistributor = eventDistributor;
        _buyLiquidContainerTaskFactory = buyLiquidContainerTaskFactory;
        _fillLiquidContainerTaskProvider = fillLiquidContainerTaskProvider;
        _drinkTaskProvider = drinkTaskProvider;
        _waterContainer = personProperties.getLiquidContainer();

        _command = new InventoryCommand();
    }

    @Override
    public Command pulse() {
        if (isInitializing()) {
            run();
        }

        if (isTerminated())
            return null;

        for (Task task : _subtasks) {
            Command command = task.pulse();
            if (command != EMPTY_COMMAND) {
                return command;
            }

            if (task.isInitializing())
                break;
        }

        for (Iterator<Task> taskIterator = _subtasks.iterator(); taskIterator.hasNext();) {
            Task task = taskIterator.next();
            if(task.isTerminated())
                taskIterator.remove();
        }

        final Command command = _command;
        _command = null;

        return command;
    }

    @Override
    public void inventory(String[] items) {
        for (String item : items) {
            if (item.equals(_waterContainer)) {
                _command = new ExamineItemCommand(_waterContainer);
                return;
            }
        }

        AbstractTask buyLiquidContainerTask = _buyLiquidContainerTaskFactory.create(new BuyContainerCallback());
        _subtasks.add(buyLiquidContainerTask);
        _eventDistributor.subscribe(buyLiquidContainerTask);
    }

    @Override
    public void examineWaterContainer(final LiquidContainer.State state) {
        if (state == LiquidContainer.State.EMPTY || state == LiquidContainer.State.LESS_THAN_HALF) {
            final FillLiquidContainerTask task = _fillLiquidContainerTaskProvider.create(new FillContainerCallback());
            _subtasks.add(task);
            _eventDistributor.subscribe(task);
        } else {
            //TODO prove, that drink task doesn't exist already
            final DrinkTask drinkTask = _drinkTaskProvider.create(new DrinkTaskCallback());
            _subtasks.add(drinkTask);
            _eventDistributor.subscribe(drinkTask);
        }
    }

    private class BuyContainerCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {
            _command = new InventoryCommand();
        }

        @Override
        public void failed() {
            fail();
        }
    }

    private class FillContainerCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {
            final DrinkTask drinkTask = _drinkTaskProvider.create(new DrinkTaskCallback());
            _subtasks.add(drinkTask);
            _eventDistributor.subscribe(drinkTask);
        }

        @Override
        public void failed() {
            fail();
        }
    }

    private class DrinkTaskCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {
        }

        @Override
        public void failed() {
        }
    }
}
