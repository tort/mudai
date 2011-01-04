package com.tort.mudai.mapper;

import com.tort.mudai.event.Event;

public class MockMapper implements Mapper {
    @Override
    public String getPathTo(final String location) {
        return "свсссзсвю";
    }

    @Override
    public void handle(final Event e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
