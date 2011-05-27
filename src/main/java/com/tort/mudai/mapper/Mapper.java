package com.tort.mudai.mapper;

import com.db4o.ObjectSet;

import java.util.List;
import java.util.Set;

public interface Mapper {
    List<Direction> pathTo(String location);
    Location currentLocation();
}
