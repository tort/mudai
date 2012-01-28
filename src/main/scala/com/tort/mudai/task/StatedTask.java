package com.tort.mudai.task;

public abstract class StatedTask extends AbstractTask {
    private volatile Status _status;

    public StatedTask() {
        Status oldStatus = _status;
        _status = Status.NEW;
        logStateChange(oldStatus);
    }

    protected void run() {
        if (_status != Status.NEW)
            throw new IllegalStateException("cannot apply when task is not initializing");

        Status oldStatus = _status;
        _status = Status.RUNNING;
        logStateChange(oldStatus);
    }


    @Override
    public boolean isInitializing() {
        return _status == Status.NEW;
    }

    @Override
    public boolean isTerminated() {
        return _status == Status.SUCCEEDED || _status == Status.FAILED;
    }
    
    @Override
    public boolean isPaused() {
        return _status == Status.PAUSED;
    }

    private void logStateChange(Status oldStatus) {
        if (_status != oldStatus) {
            System.out.println("TASK " + getClass().getName() + " entered " + _status + " state");
        }
    }

    @Override
    public void succeed() {
        Status oldStatus = _status;
        _status = Status.SUCCEEDED;
        logStateChange(oldStatus);
    }

    @Override
    public void fail() {
        Status oldStatus = _status;
        _status = Status.FAILED;
        logStateChange(oldStatus);
    }

    @Override
    public boolean isFailed() {
        return _status == Status.FAILED;
    }

    @Override
    public boolean isSucceeded() {
        return _status == Status.SUCCEEDED;
    }

    @Override
    public boolean isRunning() {
        return _status == Status.RUNNING;
    }

    @Override
    public void pause() {
        Status oldStatus = _status;
        _status = Status.PAUSED;
        logStateChange(oldStatus);
    }

    @Override
    public void resume() {
        Status oldStatus = _status;
        _status = Status.RUNNING;
        logStateChange(oldStatus);
    }
}
