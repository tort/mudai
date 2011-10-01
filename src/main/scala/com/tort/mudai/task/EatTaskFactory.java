package com.tort.mudai.task;

public interface EatTaskFactory {
    EatTask create(TaskTerminateCallback callback);
}
