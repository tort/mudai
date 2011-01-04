package com.tort.mudai.event;

public class MoveEvent implements Event {
    private final String _direction;

    public MoveEvent(final String direction) {
        _direction = direction;
    }

    public String getDirection() {
        return _direction;
    }
}
