package com.tort.mudai.mapper;

public enum Directions {
    WEST("запад", "W"),
    EAST("восток", "E"),
    NORTH("север", "N"),
    SOUTH("юг", "S"),
    UP("вверх", "U"),
    DOWN("вниз", "D");

    private final String _name;
    private String _alias;

    private Directions(String name, String alias) {
        _name = name;
        _alias = alias;
    }

    public String getName() {
        return _name;
    }

    public String getAlias() {
        return _alias;
    }
}
