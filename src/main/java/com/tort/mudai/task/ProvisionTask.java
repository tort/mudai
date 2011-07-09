package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.ExamineItemCommand;
import com.tort.mudai.command.InventoryCommand;

public class ProvisionTask extends StatedTask {
    private volatile Command _command;
    private final EventDistributor _eventDistributor;
    private final Provider<BuyLiquidContainerTask> _buyLiquidContainerTaskProvider;
    private final Provider<FillLiquidContainerTask> _fillLiquidContainerTaskProvider;
    private final Provider<DrinkTask> _drinkTaskProvider;
    private final String _waterContainer;

    private BuyLiquidContainerTask _buyLiquidContainerTask;

    @Inject
    public ProvisionTask(final EventDistributor eventDistributor,
                         final Provider<BuyLiquidContainerTask> goAndByWaterContainerTaskProvider,
                         final Provider<FillLiquidContainerTask> fillLiquidContainerTaskProvider,
                         final Provider<DrinkTask> drinkTaskProvider,
                         final String waterContainer) {
        _eventDistributor = eventDistributor;
        _buyLiquidContainerTaskProvider = goAndByWaterContainerTaskProvider;
        _fillLiquidContainerTaskProvider = fillLiquidContainerTaskProvider;
        _drinkTaskProvider = drinkTaskProvider;
        _waterContainer = waterContainer;
        _command = new InventoryCommand();
    }

    @Override
    public Command pulse() {
        if (_buyLiquidContainerTask != null) {
            if (_buyLiquidContainerTask.isTerminated()) {
                if (_buyLiquidContainerTask.isFailed()) {
                    fail();
                    return null;
                }
                _buyLiquidContainerTask = null;
            }
            return _buyLiquidContainerTask.pulse();
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

        _buyLiquidContainerTask = _buyLiquidContainerTaskProvider.get();
        _eventDistributor.subscribe(_buyLiquidContainerTask);
    }

    @Override
    public Status status() {
        return Task.Status.RUNNING;
    }

    @Override
    public void waterContainerFull() {
        _eventDistributor.subscribe(_drinkTaskProvider.get());
    }

    @Override
    public void waterContainerAlmostEmpty() {
        _eventDistributor.subscribe(_fillLiquidContainerTaskProvider.get());
    }
}
