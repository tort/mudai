package com.tort.mudai.mapper;

import java.util.HashMap;
import java.util.Map;

class DirectionHelper {
    private Map<String, String> _directions = new HashMap<String, String>();

    public void invoke() {
        _directions.put(Directions.WEST.name(), Directions.EAST.name());
        _directions.put(Directions.EAST.name(), Directions.WEST.name());
        _directions.put(Directions.NORTH.name(), Directions.SOUTH.name());
        _directions.put(Directions.SOUTH.name(), Directions.NORTH.name());
        _directions.put(Directions.UP.name(), Directions.DOWN.name());
        _directions.put(Directions.DOWN.name(), Directions.UP.name());
    }

    public String getOppositeDirection(final String direction) {
        return _directions.get(direction);
    }
}
