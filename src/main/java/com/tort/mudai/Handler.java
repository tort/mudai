package com.tort.mudai;

import com.tort.mudai.event.Event;
import com.tort.mudai.task.AbstractTask;

public interface Handler<E extends Event> {
    void handle(AbstractTask task);
}
