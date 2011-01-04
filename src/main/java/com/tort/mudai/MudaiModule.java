package com.tort.mudai;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.tort.mudai.command.Command;

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
    }
}
