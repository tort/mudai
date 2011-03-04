package com.tort.mudai.mapper;

import com.tort.mudai.AdapterEventListener;

import java.util.List;

public interface Mapper extends AdapterEventListener {
    String getPathTo(String location);

    List<String> knownLocations();
}
