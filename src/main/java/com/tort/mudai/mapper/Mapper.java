package com.tort.mudai.mapper;

import com.tort.mudai.AdapterEventListener;

import java.util.List;

public interface Mapper extends AdapterEventListener {
    List<Direction> pathTo(String location);

    List<String> knownLocations();
}
