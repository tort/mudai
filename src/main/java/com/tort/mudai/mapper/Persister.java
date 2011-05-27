package com.tort.mudai.mapper;

import com.db4o.ObjectSet;

import java.util.List;

public interface Persister {
    void persistLocation(Location current);

    Location loadLocation(String title);

    List<Location> enlistLocations();

    Mob findOrCreateMob(String mobLongName);

    ObjectSet<Mob> enlistMobs();
}
