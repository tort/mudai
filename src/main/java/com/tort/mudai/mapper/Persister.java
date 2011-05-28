package com.tort.mudai.mapper;

import com.db4o.ObjectSet;

import java.util.List;

public interface Persister {
    void persistLocation(Location current);

    Location loadLocation(String title);

    List<Location> enlistLocations();

    /**
     * find mob by long name, or create new
     * @param mobLongName
     * @return mob, found or created
     */
    Mob findOrCreateMob(String mobLongName);

    ObjectSet<Mob> enlistMobs();
}
