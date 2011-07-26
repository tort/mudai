package com.tort.mudai;

import com.tort.mudai.mapper.Directions;

import java.util.ArrayList;
import java.util.List;

public class RoomSnapshot {
    private String _locationTitle;
    private String[] _objectsPresent;
    private String[] _mobs;
    private List<Directions> _doors = new ArrayList<Directions>();

    public void setLocationTitle(String locationTitle) {
        _locationTitle = locationTitle;
    }

    public void setObjectsPresent(String[] objectsPresent) {
        _objectsPresent = objectsPresent;
    }

    public String getLocationTitle() {
        return _locationTitle;
    }

    public String[] getObjectsPresent() {
        return _objectsPresent;
    }

    public void setMobs(String[] _mobs) {
        this._mobs = _mobs;
    }

    public String[] getMobs() {
        return _mobs;
    }

    public void addDoor(Directions door) {
        _doors.add(door);
    }
}
