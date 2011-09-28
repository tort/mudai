package com.tort.mudai.event;

public class LookAroundEvent implements Event {

    private String[] _objects;
    private String[] _mobs;

    public void setObjects(final String[] objects) {
        _objects = objects;
    }

    public String[] getObjects() {
        return _objects;
    }

    public void setMobs(final String[] mobs) {
        _mobs = mobs;
    }

    public String[] getMobs() {
        return _mobs;
    }
}
