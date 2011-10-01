package com.tort.mudai.task;

import com.tort.mudai.mapper.Location;

public interface TravelTaskFactory {
    TravelTask create(Location to, TaskTerminateCallback callback);
}
