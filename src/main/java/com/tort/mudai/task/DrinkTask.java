package com.tort.mudai.task;

import com.tort.mudai.command.Command;

public class DrinkTask extends AbstractTask {
    @Override
    public Command pulse() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Status status() {
        //TODO pull up
        return Task.Status.RUNNING;
    }
}
