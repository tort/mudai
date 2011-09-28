package com.tort.mudai.mapper;

import com.db4o.ObjectSet;

import java.util.List;

public interface Persister {
    void persistLocation(Location current);

    List<Location> loadLocation(Location prototype);

    List<Location> enlistLocations();

    /**
     * find mob by long name, or create new
     * @param mobLongName
     * @return mob, found or created
     */
    Mob findOrCreateMob(String mobLongName);

    ObjectSet<Mob> enlistMobs();

    void mob(String mob);

    Mob findMob(String mobLongName);

    void persistMob(Mob mob);
}
