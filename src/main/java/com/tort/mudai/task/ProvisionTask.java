package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.ExamineItemCommand;
import com.tort.mudai.command.InventoryCommand;
import com.tort.mudai.event.LiquidContainer;

public class ProvisionTask extends StatedTask {
    private volatile Command _command;
    private final EventDistributor _eventDistributor;
    private final Provider<BuyLiquidContainerTask> _buyLiquidContainerTaskProvider;
    private final Provider<FillLiquidContainerTask> _fillLiquidContainerTaskProvider;
    private final Provider<DrinkTask> _drinkTaskProvider;
    private final String _waterContainer;

    private BuyLiquidContainerTask _buyLiquidContainerTask;
    private DrinkTask _drinkTask;
    private FillLiquidContainerTask _fillLiquidContainerTask;

    @Inject
    public ProvisionTask(final EventDistributor eventDistributor,
                         final Provider<BuyLiquidContainerTask> goAndByWaterContainerTaskProvider,
                         final Provider<FillLiquidContainerTask> fillLiquidContainerTaskProvider,
                         final Provider<DrinkTask> drinkTaskProvider,
                         final PersonProperties personProperties) {
        _eventDistributor = eventDistributor;
        _buyLiquidContainerTaskProvider = goAndByWaterContainerTaskProvider;
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

        if (_buyLiquidContainerTask != null) {
            if (_buyLiquidContainerTask.isTerminated()) {
                if (_buyLiquidContainerTask.isFailed()) {
                    fail();
                    return null;
                }
                _buyLiquidContainerTask = null;
            } else {
                return _buyLiquidContainerTask.pulse();
            }
        }

        if (_fillLiquidContainerTask != null) {
            if (_fillLiquidContainerTask.isTerminated()) {
                if (_fillLiquidContainerTask.isFailed()) {
                    fail();
                    return null;
                } else if (_fillLiquidContainerTask.isSucceeded()){
                    if(_drinkTask == null){
                        _drinkTask = _drinkTaskProvider.get();
                        _eventDistributor.subscribe(_drinkTask);
                    }
                }
                _fillLiquidContainerTask = null;
            } else {
                return _fillLiquidContainerTask.pulse();
            }
        }

        if (_drinkTask != null) {
            if (_drinkTask.isTerminated()) {
                _drinkTask = null;
            } else {
                final Command command = _drinkTask.pulse();
                if (command != null)
                    return command;
            }
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
    public void examineWaterContainer(final LiquidContainer.State state) {
        if (state == LiquidContainer.State.EMPTY || state == LiquidContainer.State.LESS_THAN_HALF) {
            _fillLiquidContainerTask = _fillLiquidContainerTaskProvider.get();
            _eventDistributor.subscribe(_fillLiquidContainerTask);
        } else {
            if (_drinkTask == null) {
                _drinkTask = _drinkTaskProvider.get();
                _eventDistributor.subscribe(_drinkTask);
            }
        }
    }
}
