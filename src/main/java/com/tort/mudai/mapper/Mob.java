package com.tort.mudai.mapper;

import java.util.HashSet;
import java.util.Set;

public class Mob {
    private String _name;
    private String _descName;
    private Set<Location> _habitationArea = new HashSet<Location>();

    public Mob() {
    }

    public String getName() {
        return _name;
    }

    public void updateHabitationArea(Location current) {
        _habitationArea.add(current);
    }

    public void setName(String name) {
        _name = name;
    }

    public Set<Location> habitationArea() {
        return _habitationArea;
    }

    public String getDescName() {
        return _descName != null ? _descName : _name;
    }

    public void setDescName(final String descName) {
        _descName = descName;
    }
}
