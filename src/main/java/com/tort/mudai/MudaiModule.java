package com.tort.mudai;

import com.db4o.Db4oEmbedded;
import com.db4o.EmbeddedObjectContainer;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.*;
import com.tort.mudai.task.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.concurrent.*;

public class MudaiModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().implement(Task.class, TravelTask.class).build(TravelTaskFactory.class));
        install(new FactoryModuleBuilder().implement(Task.class, GoAndDoTask.class).build(GoAndDoTaskFactory.class));
        install(new FactoryModuleBuilder().implement(Task.class, BuyLiquidContainerTask.class).build(BuyLiquidContainerTaskFactory.class));

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
        bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(5));
        bind(new TypeLiteral<DirectedGraph<Location, Direction>>(){}).toInstance(new DefaultDirectedGraph<Location, Direction>(Direction.class));
        bind(Persister.class).to(Db4oPersister.class).in(Scopes.SINGLETON);

        final EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
        configuration.common().objectClass(Location.class).cascadeOnUpdate(true);
        final EmbeddedObjectContainer db = Db4oEmbedded.openFile(configuration, "mapper.db");
        bind(ObjectContainer.class).toInstance(db);
    }
}
