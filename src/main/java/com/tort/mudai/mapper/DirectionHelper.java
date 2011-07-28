package com.tort.mudai.mapper;

import java.util.HashMap;
import java.util.Map;

class DirectionHelper {
    private Map<String, String> _directions = new HashMap<String, String>();

    public DirectionHelper(){
        _directions.put(Directions.WEST.getName(), Directions.EAST.getName());
        _directions.put(Directions.EAST.getName(), Directions.WEST.getName());
        _directions.put(Directions.NORTH.getName(), Directions.SOUTH.getName());
        _directions.put(Directions.SOUTH.getName(), Directions.NORTH.getName());
        _directions.put(Directions.UP.getName(), Directions.DOWN.getName());
        _directions.put(Directions.DOWN.getName(), Directions.UP.getName());
    }

    public void invoke() {
    }

    public String getOppositeDirection(final String direction) {
        return _directions.get(direction);
    }
}
