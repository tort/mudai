package com.tort.mudai.mapper;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.Task;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;

import java.util.*;

@SuppressWarnings({"UnusedDeclaration"})
public class MapperImpl extends AbstractTask implements Mapper {
    private DirectedGraph<Location, Direction> _graph;
    private volatile Location _current;
    private LocationHelper _locationHelper;
    private Persister _persister;
    private Map<String, String> _directions = new HashMap<String, String>();

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
    public void lookAround(String locationTitle) {
        if (_current == null) {
            final String title = locationTitle;
            _current = _persister.loadLocation(title);
            if (_current == null) {
                _current = new Location();
                _current.setTitle(title);
                _persister.persistLocation(_current);
                _graph.addVertex(_current);
            }
        } else {
            if (_current.getTitle() == null) {
                _current.setTitle(locationTitle);
                _persister.persistLocation(_current);
            }
        }
    }

    @Inject
    public MapperImpl(final DirectedGraph<Location, Direction> graph, final Persister persister) {
        _graph = graph;
        _persister = persister;

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
    public List<Direction> pathTo(final String locationTitle) {
        final Location target = _persister.loadLocation(locationTitle);
        if (target == null)
            return null;

        final DijkstraShortestPath<Location, Direction> _algorythm = new DijkstraShortestPath<Location, Direction>(_graph, _current, target);
        final List<Direction> directions = _algorythm.getPathEdgeList();

        return directions;
    }

    @Override
    public List<String> knownLocations() {
        final List<String> result = new ArrayList();

        final List<Location> locations = _persister.enlistLocations();
        for (Location location : locations) {
            result.add(location.getTitle());
            _persister.persistLocation(location);
        }

        return result;
    }

    @Override
    public Command pulse() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Status status() {
        return Status.RUNNING;
    }

    private String getOppositeDirection(final String direction) {
        return _directions.get(direction);
    }

    @Override
    public Location currentLocation() {
        return _current;
    }
}
