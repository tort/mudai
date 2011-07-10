package com.tort.mudai.mapper;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.google.inject.Inject;
import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.task.StatedTask;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"UnusedDeclaration"})
public class MapperImpl extends StatedTask implements Mapper {
    private DirectedGraph<Location, Direction> _graph;
    private volatile Location _current;
    private LocationHelper _locationHelper;
    private Persister _persister;
    private Map<String, String> _directions = new HashMap<String, String>();
    private final ObjectContainer _db;

    @Override
    public void move(String direction) {
        final String oppositeDirection = getOppositeDirection(direction);
        Location location = _current.getByDirection(direction );
        if (location == null) {
            final Location newLocation = new Location();
            _current.addDirection(direction , newLocation);
            newLocation.addDirection(oppositeDirection, _current);
            _graph.addVertex(newLocation);
            _graph.addEdge(_current, newLocation, new Direction(direction ));
            _graph.addEdge(newLocation, _current, new Direction(oppositeDirection));
            _persister.persistLocation(newLocation);
            _persister.persistLocation(_current);
            _current = newLocation;
            System.out.println("NEW ROOM");
        } else {
            System.out.println("ROOM: " + location.getTitle());
            _current = location;
        }
    }

    @Override
    public void lookAround(RoomSnapshot roomSnapshot) {
        updateMap(roomSnapshot);
        updateMobs(roomSnapshot);
    }

    private void updateMobs(RoomSnapshot roomSnapshot) {
        for (String mobLongName : roomSnapshot.getMobs()) {
            Mob mob = _persister.findOrCreateMob(mobLongName);
            mob.updateHabitationArea(_current);
        }
    }

    private void updateMap(RoomSnapshot roomSnapshot) {
        String locationTitle = roomSnapshot.getLocationTitle();
        if (_current == null) {
            _current = _persister.loadLocation(locationTitle);
            if (_current == null) {
                _current = new Location();
                _current.setTitle(locationTitle);
                _persister.persistLocation(_current);
                _graph.addVertex(_current);
            }
        } else {
            if (_current.getTitle() == null) {
                _current.setTitle(locationTitle);
                _persister.persistLocation(_current);
            }
        }

        if(isInitializing()){
            run();
        }
    }

    @Inject
    public MapperImpl(final DirectedGraph<Location, Direction> graph, final Persister persister, final ObjectContainer db) {
        _graph = graph;
        _persister = persister;
        _db = db;

        _directions.put("запад", "восток");
        _directions.put("восток", "запад");
        _directions.put("север", "юг");
        _directions.put("юг", "север");
        _directions.put("вверх", "вниз");
        _directions.put("вниз", "вверх");

        final List<Location> locations = _persister.enlistLocations();

        for (Location location : locations) {
            _graph.addVertex(location);
        }

        for (Location location : locations) {
            final Set<String> directions = location.getDirections();
            for (String direction : directions) {
                _graph.addEdge(location, location.getByDirection(direction), new Direction(direction));
            }
        }
    }

    @Override
    public List<Direction> pathTo(final String locationTitle) throws MapperException {
        final Location target = _persister.loadLocation(locationTitle);
        if (target == null)
            throw new MapperException("location not found");

        return pathTo(target);
    }

    @Override
    public List<Direction> pathTo(final Location target) {
        final DijkstraShortestPath<Location, Direction> _algorythm = new DijkstraShortestPath<Location, Direction>(_graph, _current, target);
        final List<Direction> directions = _algorythm.getPathEdgeList();

        return directions;
    }

    private String getOppositeDirection(final String direction) {
        return _directions.get(direction);
    }

    @Override
    public Location currentLocation() {
        return _current;
    }

    @Override
    public void markWaterSource(final String waterSource) {
        final Location location = currentLocation();
        location.setWaterSource(waterSource);
        _db.store(location);
        _db.commit();
    }

    @Override
    public List<Direction> pathToNearestWaterSource() {
        final ObjectSet<Location> locations = _db.query(new Predicate<Location>() {
            @Override
            public boolean match(final Location location) {
                return location.getWaterSource() != null;
            }
        });

        final List<Direction> directions = pathTo(locations.get(0));
        return directions;
    }

    @Override
    public Location nearestWaterSource() throws MapperException {
        final ObjectSet<Location> locations = _db.query(new Predicate<Location>() {
            @Override
            public boolean match(final Location location) {
                return location.getWaterSource() != null;
            }
        });
        if(locations.isEmpty()){
            throw new MapperException("NO WATER SOURCES EXIST");
        }

        return locations.get(0);
    }

}
