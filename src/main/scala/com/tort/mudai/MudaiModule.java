package com.tort.mudai;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.tort.mudai.command.RenderableCommand;
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
        install(new FactoryModuleBuilder().implement(Task.class, FillLiquidContainerTask.class).build(FillLiquidContainerTaskFactory.class));
        install(new FactoryModuleBuilder().implement(Task.class, DrinkTask.class).build(DrinkTaskFactory.class));
        install(new FactoryModuleBuilder().implement(Task.class, EatTask.class).build(EatTaskFactory.class));

        bind(AbstractTask.class).annotatedWith(Names.named("mapperTask")).to(MapperImpl.class);
        bind(Mapper.class).to(MapperImpl.class);
        bind(MapperImpl.class).in(Scopes.SINGLETON);

        bind(Adapter.class).to(AdapterImpl.class);
        bind(CommandExecutor.class).to(AdapterImpl.class);
        bind(AdapterImpl.class).in(Scopes.SINGLETON);

        bind(Person.class).in(Scopes.SINGLETON);
        bind(EventDistributor.class).in(Scopes.SINGLETON);
        bind(Persister.class).toInstance(new MemPersister());

        bind(new TypeLiteral<BlockingQueue<RenderableCommand>>(){}).to(new TypeLiteral<PriorityBlockingQueue<RenderableCommand>>(){}).in(Scopes.SINGLETON);
        bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(20));
        bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(20));
        bind(new TypeLiteral<DirectedGraph<Location, Direction>>(){}).toInstance(new DefaultDirectedGraph<Location, Direction>(Direction.class));
    }
}
