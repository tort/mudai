package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.ExamineItemCommand;
import com.tort.mudai.command.InventoryCommand;

public class ProvisionTask extends AbstractTask {
    private volatile Command _command;
    private final EventDistributor _eventDistributor;
    private final Provider<GoAndBuyWaterContainerTask> _goAndByWaterContainerTaskProvider;
    private final String _waterContainer;
    private GoAndBuyWaterContainerTask _buyWaterTask;

    @Inject
    public ProvisionTask(final EventDistributor eventDistributor, final Provider<GoAndBuyWaterContainerTask> goAndByWaterContainerTaskProvider, final String waterContainer) {
        _eventDistributor = eventDistributor;
        _goAndByWaterContainerTaskProvider = goAndByWaterContainerTaskProvider;
        _waterContainer = waterContainer;
        _command = new InventoryCommand();

        System.out.println("PROVISION_TASK: started");
    }

    @Override
    public Command pulse() {
        if(_buyWaterTask != null){
            if(_buyWaterTask.status() == Status.TERMINATED){
                _buyWaterTask = null;
                return new ExamineItemCommand(_waterContainer);
            }
            return _buyWaterTask.pulse();
        }

        final Command command = _command;
        _command = null;

        return command;
    }

    @Override
    public void inventory(String[] items) {
        for (String item : items) {
            if(item.equals(_waterContainer)){

            }
        }

        _buyWaterTask = _goAndByWaterContainerTaskProvider.get();
        _eventDistributor.subscribe(_buyWaterTask);
    }

    @Override
    public Status status() {
        return Task.Status.RUNNING;
    }
}
