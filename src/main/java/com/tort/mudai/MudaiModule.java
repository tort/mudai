package com.tort.mudai;

import com.google.inject.*;
import com.tort.mudai.command.Command;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class MudaiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Adapter.class).to(AdapterImpl.class).in(Scopes.SINGLETON);
        bind(BlockingQueue.class).to(PriorityBlockingQueue.class).in(Scopes.SINGLETON);
        bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(2));
    }
}
