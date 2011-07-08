package com.tort.mudai.mapper;

import java.util.List;

public interface Mapper {
    List<Direction> pathTo(String location);
    Location currentLocation();
    void markWaterSource(String waterSource);
    //TODO remove unused
    List<Direction> pathToNearestWaterSource();
    String nearestWaterSource();
}
