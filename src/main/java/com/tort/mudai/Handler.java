package com.tort.mudai;

import com.tort.mudai.event.Event;
import com.tort.mudai.task.Task;

public interface Handler<E extends Event> {
    void handle(Task task, E event);
}
