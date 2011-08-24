package com.tort.mudai.mapper;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.google.inject.Inject;

import java.util.List;

public class Db4oPersister implements Persister {
    private final ObjectContainer _db;

    @Inject
    public Db4oPersister(final ObjectContainer db) {
        _db = db;
    }

    @Override
    public void persistLocation(final Location location) {
        _db.store(location);
        _db.commit();
    }

    @Override
    public List<Location> loadLocation(Location prototype) {
        final ObjectSet<Location> locations = _db.queryByExample(prototype);

        return locations;
    }

    @Override
    public List<Location> enlistLocations() {
        return _db.queryByExample(Location.class);
    }

    @Override
    public Mob findOrCreateMob(final String mobLongName) {
        ObjectSet<Mob> query = _db.query(new Predicate<Mob>() {
            @Override
            public boolean match(Mob mob) {
                return mob.getLongName().equals(mobLongName);
            }
        });

        if(query.size() > 1)
            throw new IllegalStateException("two mobs with same long name found");

        Mob mob = null;
        
        if(query.size() == 1){
            mob = query.get(0);
        }

        if(query.size() < 1){
            mob = new Mob();
            mob.setName(mobLongName);
            _db.store(mob);
        }

        return mob;
    }

    @Override
    public ObjectSet<Mob> enlistMobs() {
        return _db.query(Mob.class);
    }
}
