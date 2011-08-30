package com.tort.mudai.mapper;

import com.sun.xml.internal.bind.v2.util.QNameMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Location {
    private String _title;
    private Map<String, Location> _directions = new HashMap<String, Location>();
    private String _waterSource;
    private boolean _isShop = false;
    private boolean _isTavern = false;
    private String _desc;
    private Set<Directions> _availableExits;
    private Set _knownMobs = new HashSet();

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

    public void markShop() {
        _isShop = true;
    }

    public void markTavern() {
        _isTavern = true;
    }

    public boolean isShop() {
        return _isShop;
    }

    public boolean isTavern() {
        return _isTavern;
    }

    public void setDesc(String desc) {
        _desc = desc;
    }

    public String getDesc() {
        return _desc;
    }

    public void setAvailableExits(Set<Directions> availableExits) {
        _availableExits = availableExits;
    }

    public Set<Directions> getAvailableExits() {
        return _availableExits;
    }

    public void discoverMob(String mob) {
        _knownMobs.add(mob);
    }
}
