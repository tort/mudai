package com.tort.mudai;

import com.tort.mudai.event.Event;

public interface Handler<T extends Event> {
    void handle(T event) throws InterruptedException;
}
