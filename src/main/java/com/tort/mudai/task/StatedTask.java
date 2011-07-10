package com.tort.mudai.task;

public abstract class StatedTask extends AbstractTask {
    private Status _status;

    public StatedTask(){
        _status = Status.INIT;
        logStateChange();
    }

    protected void run(){
        if(_status != Status.INIT)
            throw new IllegalStateException("cannot apply when task is not initializing");

        _status = Status.RUNNING;
        logStateChange();
    }

    @Override
    public boolean isInitializing() {
        return _status == Status.INIT;
    }

    @Override
    public boolean isTerminated() {
        return _status == Status.SUCCESS || _status == Status.FAIL;
    }

    private void logStateChange() {
        System.out.println("TASK " + getClass().getName() + " entered " + _status + " state");
    }

    @Override
    public void succeed() {
        _status = Status.SUCCESS;
        logStateChange();
    }

    @Override
    public void fail(){
        _status = Status.FAIL;
        logStateChange();
    }

    @Override
    public boolean isFailed(){
        return _status == Status.FAIL;
    }
}
