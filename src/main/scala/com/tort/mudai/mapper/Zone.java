package com.tort.mudai.mapper;

public class Zone {
    private String _name;

    public Zone(){

    }

    public Zone(String zoneName){
        _name = zoneName;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }
}
