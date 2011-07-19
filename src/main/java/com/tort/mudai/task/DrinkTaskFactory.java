package com.tort.mudai.task;

public interface DrinkTaskFactory {
    DrinkTask create(TaskTerminateCallback callback);
}
