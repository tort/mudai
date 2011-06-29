package com.tort.mudai.task;

import com.tort.mudai.command.Command;

public class GoAndBuyWaterContainerTask extends StatedTask {
    public GoAndBuyWaterContainerTask() {
        System.out.println("GO_AND_BUY: started");
    }

    @Override
    public Command pulse() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Status status() {
        return Task.Status.RUNNING;
    }
}
