package com.tort.mudai.mapper;

import java.util.List;

public interface Mapper {
    List<Direction> pathTo(String location) throws MapperException;
    Location currentLocation();
    void markWaterSource(String waterSource);
    //TODO remove unused
    List<Direction> pathToNearestWaterSource();
    Location nearestWaterSource() throws MapperException;
    List<Direction> pathTo(Location target);
    Location nearestShop() throws MapperException;
    Location nearestTavern() throws MapperException;
}
