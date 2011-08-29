package com.tort.mudai.mapper;

public enum Directions {
    WEST("запад", "З"),
    EAST("восток", "В"),
    NORTH("север", "С"),
    SOUTH("юг", "Ю"),
    UP("вверх", "^"),
    DOWN("вниз", "v");

    private final String _name;
    private CharSequence _alias;

    private Directions(String name, CharSequence alias) {
        _name = name;
        _alias = alias;
    }

    public String getName() {
        return _name;
    }

    public CharSequence getAlias() {
        return _alias;
    }
}
