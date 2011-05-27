package com.tort.mudai;

public class RoomSnapshot {
    private String _locationTitle;
    private String _objectsPresent;

    public void setLocationTitle(String locationTitle) {
        _locationTitle = locationTitle;
    }

    public void setObjectsPresent(String objectsPresent) {
        _objectsPresent = objectsPresent;
    }

    public String getLocationTitle() {
        return _locationTitle;
    }

    public String getObjectsPresent() {
        return _objectsPresent;
    }
}
