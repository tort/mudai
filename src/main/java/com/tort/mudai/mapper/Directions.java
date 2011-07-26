package com.tort.mudai.mapper;

public enum Directions {
    WEST("запад"),
    EAST("восток"),
    NORTH("север"),
    SOUTH("юг"),
    UP("вверх"),
    DOWN("вниз");

    private final String _name;

    private Directions(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }
}
