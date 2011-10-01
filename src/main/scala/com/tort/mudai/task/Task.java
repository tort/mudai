package com.tort.mudai.task;

import com.tort.mudai.command.RenderableCommand;

public interface Task {
    RenderableCommand pulse();

    boolean isInitializing();

    boolean isTerminated();

    void pause();

    void resume();

    void succeed();

    void fail();

    boolean isFailed();

    boolean isSucceeded();

    public enum Status{
        NEW,
        RUNNABLE,
        PAUSED,
        SUCCEEDED,
        FAILED
    }
}
