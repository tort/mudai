package com.tort.mudai.mapper;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

@Test
public class MapperImplTest {
    public void getPath(){
        DirectedGraph<Location, String> _graph = new DefaultDirectedGraph(String.class);

        final Location loc1 = new Location();
        final Location loc2 = new Location();
        final Location loc3 = new Location();

        _graph.addVertex(loc1);
        _graph.addVertex(loc2);
        _graph.addVertex(loc3);

        _graph.addEdge(loc1, loc2, "east1");
        _graph.addEdge(loc2, loc1, "west1");
        _graph.addEdge(loc2, loc3, "east2");
        _graph.addEdge(loc3, loc2, "west2");

        final DijkstraShortestPath<Location, String> _algorythm = new DijkstraShortestPath<Location, String>(_graph, loc1, loc3);
        assertEquals(_algorythm.getPathEdgeList().size(), 2);
    }

    public void getPathToUnknownRoom(){
        final DirectedGraph<Location, Direction> graph = new DefaultDirectedGraph(String.class);
        final Persister persister = new MockPersister();
        final MapperImpl mapper = new MapperImpl(graph, persister, null);

        final List<Direction> pathTo;
        try {
            pathTo = mapper.pathTo("unknown");
            fail("must throw MapperException in case of unknown destination");
        } catch (MapperException e) {
        }
    }

    public void nearestWaterSource(){

    }
}
