package com.tort.mudai.mapper;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import org.jgrapht.DirectedGraph;

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
}
