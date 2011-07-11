package com.tort.mudai.task;

import com.tort.mudai.command.Command;

public interface Task {
    Command pulse();

    boolean isInitializing();

    boolean isTerminated();

    void succeed();

    void fail();

    boolean isFailed();

    boolean isSucceeded();

    public enum Status{
        INIT,
        RUNNING,
        SUCCESS,
        FAIL
    }
}
