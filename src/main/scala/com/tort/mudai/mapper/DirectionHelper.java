package com.tort.mudai.mapper;

import java.util.HashMap;
import java.util.Map;

class DirectionHelper {
    private Map<String, String> _directionNames = new HashMap<String, String>();
    private Map<Directions, Directions> _directions = new HashMap<Directions, Directions>();

    public DirectionHelper(){
        _directionNames.put(Directions.WEST.getName(), Directions.EAST.getName());
        _directionNames.put(Directions.EAST.getName(), Directions.WEST.getName());
        _directionNames.put(Directions.NORTH.getName(), Directions.SOUTH.getName());
        _directionNames.put(Directions.SOUTH.getName(), Directions.NORTH.getName());
        _directionNames.put(Directions.UP.getName(), Directions.DOWN.getName());
        _directionNames.put(Directions.DOWN.getName(), Directions.UP.getName());

        _directions.put(Directions.WEST, Directions.EAST);
        _directions.put(Directions.EAST, Directions.WEST);
        _directions.put(Directions.NORTH, Directions.SOUTH);
        _directions.put(Directions.SOUTH, Directions.NORTH);
        _directions.put(Directions.UP, Directions.DOWN);
        _directions.put(Directions.DOWN, Directions.UP);
    }

    public void invoke() {
    }

    public String getOppositeDirectionName(final String direction) {
        return _directionNames.get(direction);
    }

    public Directions getOppositeDirection(final Directions direction) {
        return _directions.get(direction);
    }
}
