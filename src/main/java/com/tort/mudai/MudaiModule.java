package com.tort.mudai;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.*;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;
import com.tort.mudai.task.Person;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.concurrent.*;

public class MudaiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AbstractTask.class).annotatedWith(Names.named("mapperTask")).to(MapperImpl.class);
        bind(Mapper.class).to(MapperImpl.class);
        bind(MapperImpl.class).in(Scopes.SINGLETON);

        bind(Adapter.class).to(AdapterImpl.class);
        bind(CommandExecutor.class).to(AdapterImpl.class);
        bind(AdapterImpl.class).in(Scopes.SINGLETON);

        bind(Person.class).in(Scopes.SINGLETON);
        bind(EventDistributor.class).in(Scopes.SINGLETON);
        
        bind(new TypeLiteral<BlockingQueue<Command>>(){}).to(new TypeLiteral<PriorityBlockingQueue<Command>>(){}).in(Scopes.SINGLETON);
        bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(5));
        bind(ScheduledExecutorService.class).toInstance(Executors.newSingleThreadScheduledExecutor());
        bind(new TypeLiteral<DirectedGraph<Location, Direction>>(){}).toInstance(new DefaultDirectedGraph(Direction.class));
        bind(Persister.class).to(Db4oPersister.class).in(Scopes.SINGLETON);
    }
}
