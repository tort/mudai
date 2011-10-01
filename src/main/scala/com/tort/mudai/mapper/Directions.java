package com.tort.mudai.mapper;

public enum Directions {
    WEST("запад", "w", false),
    WEST_BORDER("запад", "W", true),
    EAST("восток", "e", false),
    EAST_BORDER("восток", "E", true),
    NORTH("север", "n", false),
    NORTH_BORDER("север", "N", true),
    SOUTH("юг", "s", false),
    SOUTH_BORDER("юг", "S", true),
    UP("вверх", "u", false),
    UP_BORDER("вверх", "U", true),
    DOWN("вниз", "d", false),
    DOWN_BORDER("вниз", "D", true);

    private final String _name;
    private String _alias;
    private boolean _border;

    private Directions(String name, String alias, final boolean border) {
        _name = name;
        _alias = alias;
        _border = border;
    }

    public String getName() {
        return _name;
    }

    public String getAlias() {
        return _alias;
    }

    public boolean border() {
        return _border;
    }
}
