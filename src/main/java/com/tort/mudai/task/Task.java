package com.tort.mudai.task;

import com.tort.mudai.command.Command;

public interface Task {
    Command pulse();

    Status status();

    public enum Status{
        RUNNING, TERMINATED
    }
}
