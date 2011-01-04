package com.tort.mudai;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class MudaiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Adapter.class).to(AdapterImpl.class);
        bind(CommandExecutor.class).to(AdapterImpl.class);
        bind(AdapterImpl.class).in(Scopes.SINGLETON);
        
        bind(new TypeLiteral<BlockingQueue<Command>>(){}).to(new TypeLiteral<PriorityBlockingQueue<Command>>(){}).in(Scopes.SINGLETON);
        bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(2));
        bind(Mapper.class).to(MapperImpl.class);
        bind(new TypeLiteral<DirectedGraph<Location, Direction>>(){}).toInstance(new DefaultDirectedGraph(Direction.class));
        bind(Persister.class).to(MockPersister.class);
    }
}
