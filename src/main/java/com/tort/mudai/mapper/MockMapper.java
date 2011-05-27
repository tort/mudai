package com.tort.mudai.mapper;

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
    public Location currentLocation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
