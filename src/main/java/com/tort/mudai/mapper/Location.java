package com.tort.mudai.mapper;

import com.db4o.collections.ActivatableHashMap;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class Location {
    private String _title;
    private Map<String, Location> _directions = new HashMap<String, Location>();

    @Id
    public String getTitle() {
        return _title;
    }

    public void setTitle(final String locationTitle) {
        _title = locationTitle;
    }

    public Location getByDirection(final String direction) {
        return _directions.get(direction);
    }

    public void addDirection(final String direction, final Location location) {
        _directions.put(direction, location);
    }

    public Set<String> getDirections() {
        return _directions.keySet();
    }
}
