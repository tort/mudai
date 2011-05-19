package com.tort.mudai.mapper;

import com.tort.mudai.AdapterEventListener;
import com.tort.mudai.task.Task;

import java.util.List;

public interface Mapper {
    List<Direction> pathTo(String location);
    List<String> knownLocations();
}
