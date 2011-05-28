package com.tort.mudai.mapper;

import java.util.HashSet;
import java.util.Set;

public class Mob {
    private String _name;
    private Set<Location> _habitationArea = new HashSet<Location>();

    public Mob() {
    }

    public String getLongName() {
        return _name;
    }

    public void updateHabitationArea(Location current) {
        _habitationArea.add(current);
    }

    public void setName(String _name) {
        this._name = _name;
    }
}
