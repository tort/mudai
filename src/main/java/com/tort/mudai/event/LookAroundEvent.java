package com.tort.mudai.event;

public class LookAroundEvent implements Event {
    private String _locationTitle;

    public LookAroundEvent(final String locationTitle) {
        _locationTitle = locationTitle;
    }

    public String getLocationTitle() {
        return _locationTitle;
    }

}
