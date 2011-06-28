package com.tort.mudai.task;

import com.tort.mudai.command.Command;

public interface Task {
    Command pulse();

    Status status();

    boolean isInit();

    boolean isTerminated();

    public enum Status{
        INIT,
        RUNNING,
        TERMINATED
    }
}
