package com.tort.mudai.mapper;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.task.AbstractTask;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class MapperTestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AbstractTask.class).annotatedWith(Names.named("mapperTask")).to(MapperImpl.class);
        bind(Mapper.class).to(MapperImpl.class);
        bind(MapperImpl.class).in(Scopes.SINGLETON);
        bind(Persister.class).to(MemPersister.class).in(Scopes.SINGLETON);

        bind(new TypeLiteral<BlockingQueue<RenderableCommand>>() {
        }).to(new TypeLiteral<PriorityBlockingQueue<RenderableCommand>>() {
        }).in(Scopes.SINGLETON);
        bind(new TypeLiteral<DirectedGraph<Location, Direction>>(){}).toInstance(new DefaultDirectedGraph<Location, Direction>(Direction.class));
    }
}
