package com.tort.mudai.mapper;

import com.db4o.ObjectSet;

import java.util.List;

public class MockPersister implements Persister {
    @Override
    public void persistLocation(final Location location) {
        System.out.println("PERSISTING " + location.getTitle());
    }

    @Override
    public List<Location> loadLocation(Location prototype) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Location> enlistLocations() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Mob findOrCreateMob(String mobLongName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ObjectSet<Mob> enlistMobs() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
