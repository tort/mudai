package com.tort.mudai.mapper;

import com.tort.mudai.event.Event;

import java.util.ArrayList;
import java.util.List;

public class MockMapper implements Mapper {
    @Override
    public List<Direction> pathTo(final String location) {
        final List<Direction> result = new ArrayList();
        
        for (char c : "свсссзсвю".toCharArray()) {
            result.add(new Direction(String.valueOf(c)));
        }

        return result;
    }

    @Override
    public List<String> knownLocations() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handle(final Event e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
