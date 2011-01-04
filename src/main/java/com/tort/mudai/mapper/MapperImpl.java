package com.tort.mudai.mapper;

import com.google.inject.Inject;
import com.tort.mudai.Handler;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.LookAroundEvent;
import com.tort.mudai.event.MoveEvent;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
public class MapperImpl implements Mapper {
    private DirectedGraph<Location, Direction> _graph;
    private Location _current;
    private LocationHelper _locationHelper;
    private Map<Class, Handler> _events = new HashMap<Class, Handler>();
    private Persister _persister;
    private Map<String, Location> _locations = new HashMap<String, Location>();
    private Map<String, String> _directions = new HashMap<String, String>();

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

        _events.put(LookAroundEvent.class, new LookAroundEventHandler());
        _events.put(MoveEvent.class, new MoveEventHandler());
    }

    @Override
    public String getPathTo(final String locationTitle) {
        final Location target = _locations.get(locationTitle);
        final DijkstraShortestPath<Location, Direction> _algorythm = new DijkstraShortestPath<Location, Direction>(_graph, _current, target);
        final List<Direction> directions = _algorythm.getPathEdgeList();

        StringBuilder result = new StringBuilder();
        for (Direction direction : directions) {
            result.append(direction.getName());
        }

        return result.toString();
    }

    @Override
    public void handle(final Event event) {
        final Handler handler = _events.get(event.getClass());
        if (handler == null)
            return;

        try {
            handler.handle(event);
        } catch (InterruptedException e) {
            System.out.println("error handling " + event.getClass().getName() + "\n" + e.getMessage());
        }
    }

    private class LookAroundEventHandler implements Handler<LookAroundEvent> {
        @Override
        public void handle(final LookAroundEvent event) throws InterruptedException {
            if (_current == null) {
                final String title = event.getLocationTitle();
                _current = new Location();
                _current.setTitle(title);
                _persister.persist(_current);
                _graph.addVertex(_current);
                _locations.put(title, _current);
            } else {
                if (_current.getTitle() == null) {
                    _current.setTitle(event.getLocationTitle());
                    _locations.put(_current.getTitle(), _current);
                }
            }
        }
    }

    private class MoveEventHandler implements Handler<MoveEvent> {
        @Override
        public void handle(final MoveEvent event) throws InterruptedException {
            final String direction = event.getDirection();
            final String oppositeDirection = getOppositeDirection(direction);
            Location location = _current.getByDirection(direction);
            if (location == null) {
                final Location newLocation = new Location();
                _current.addDirection(direction, newLocation);
                newLocation.addDirection(oppositeDirection, _current);
                _graph.addVertex(newLocation);
                _graph.addEdge(_current, newLocation, new Direction(direction));
                _graph.addEdge(newLocation, _current, new Direction(oppositeDirection));
                _current = newLocation;
                System.out.println("NEW ROOM");
            } else {
                System.out.println("ROOM: " + location.getTitle());
                _current = location;
            }
        }

        private String getOppositeDirection(final String direction) {
            return _directions.get(direction);
        }
    }
}
