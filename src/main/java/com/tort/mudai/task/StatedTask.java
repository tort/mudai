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

    @Override
    public Status status() {
        return _status;
    }

    private void logStateChange() {
        System.out.println("TASK " + getClass().getName() + " entered " + _status + " state");
    }

    public void succeed() {
        _status = Status.SUCCESS;
    }

    public void fail(){
        _status = Status.FAIL;
    }

    public boolean isFailed(){
        return _status == Status.FAIL;
    }
}
