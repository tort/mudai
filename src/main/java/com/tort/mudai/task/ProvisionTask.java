package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.ExamineItemCommand;
import com.tort.mudai.command.InventoryCommand;

public class ProvisionTask extends StatedTask {
    private volatile Command _command;
    private final EventDistributor _eventDistributor;
    private final Provider<RetrieveLiquidContainerTask> _goAndByWaterContainerTaskProvider;
    private final Provider<FillLiquidContainerTask> _fillLiquidContainerTaskProvider;
    private final Provider<DrinkTask> _drinkTaskProvider;
    private final String _waterContainer;

    private RetrieveLiquidContainerTask _buyLiquidTask;

    @Inject
    public ProvisionTask(final EventDistributor eventDistributor,
                         final Provider<RetrieveLiquidContainerTask> goAndByWaterContainerTaskProvider,
                         final Provider<FillLiquidContainerTask> fillLiquidContainerTaskProvider,
                         final Provider<DrinkTask> drinkTaskProvider,
                         final String waterContainer) {
        _eventDistributor = eventDistributor;
        _goAndByWaterContainerTaskProvider = goAndByWaterContainerTaskProvider;
        _fillLiquidContainerTaskProvider = fillLiquidContainerTaskProvider;
        _drinkTaskProvider = drinkTaskProvider;
        _waterContainer = waterContainer;
        _command = new InventoryCommand();
    }

    @Override
    public Command pulse() {
        if(_buyLiquidTask != null){
            if(_buyLiquidTask.isTerminated()){
                _buyLiquidTask = null;
                return new ExamineItemCommand(_waterContainer);
            }
            return _buyLiquidTask.pulse();
        }

        final Command command = _command;
        _command = null;

        return command;
    }

    @Override
    public void inventory(String[] items) {
        for (String item : items) {
            if(item.equals(_waterContainer)){
                _command = new ExamineItemCommand(_waterContainer);
                return;
            }
        }

        _buyLiquidTask = _goAndByWaterContainerTaskProvider.get();
        _eventDistributor.subscribe(_buyLiquidTask);
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
