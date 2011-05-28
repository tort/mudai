package com.tort.mudai.mapper;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;

import java.util.List;

public class Db4oPersister implements Persister {
    private final ObjectContainer _db;

    public Db4oPersister() {
        final EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
        configuration.common().objectClass(Location.class).cascadeOnUpdate(true);
        _db = Db4oEmbedded.openFile(configuration, "mapper.db");
    }

    @Override
    public void persistLocation(final Location location) {
        _db.store(location);
        _db.commit();
    }

    @Override
    public Location loadLocation(final String title) {
        final ObjectSet<Location> locations = _db.query(new Predicate<Location>() {
            @Override
            public boolean match(final Location location) {
                return location.getTitle().equals(title);
            }
        });

        if (locations.isEmpty()) {
            System.out.println("NOT FOUND LOCATION: " + title);
            return null;
        } else {
            System.out.println("FOUND " + locations.size() + " LOCATIONS " + title);
        }

        return locations.get(0);
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
