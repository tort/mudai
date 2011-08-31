package com.tort.mudai.event;

public class DiscoverObstacleEvent implements Event {
    private String _obstacle;

    public DiscoverObstacleEvent(String obstacle) {
        _obstacle = obstacle;
    }

    public String getObstacle() {
        return _obstacle;
    }
}
