package com.tort.mudai.mapper;

import com.db4o.collections.ActivatableHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Location {
    private String _title;
    private Map<String, Location> _directions = new HashMap<String, Location>();
    private String _waterSource;

    public String getTitle() {
        return _title;
    }

    public void setTitle(final String locationTitle) {
        _title = locationTitle;
    }

    public Location getByDirection(final String direction) {
        return _directions.get(direction);
    }

    public void addDirection(final String direction, final Location location) {
        _directions.put(direction, location);
    }

    public Set<String> getDirections() {
        return _directions.keySet();
    }

    public String getWaterSource() {
        return _waterSource;
    }

    public void setWaterSource(final String waterSource) {
        _waterSource = waterSource;
    }
}
