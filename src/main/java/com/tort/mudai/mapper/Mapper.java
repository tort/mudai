package com.tort.mudai.mapper;

import java.util.List;

public interface Mapper {
    List<Direction> pathTo(String location);
    List<String> knownLocations();
}
