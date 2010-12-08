package com.tort.mudai;

import com.tort.mudai.event.Event;

public interface Handler {
    void handle(Event e) throws InterruptedException;
}
