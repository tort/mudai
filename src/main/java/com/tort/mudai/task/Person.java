package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.Adapter;
import com.tort.mudai.AdapterEventListener;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.EventSource;
import com.tort.mudai.command.Command;

public class Person implements CommandExecutor, EventSource {
    private final Provider<SessionTask> _sessionProvider;
    private final Adapter _adapter;

    private SessionTask _sessionTask;

    @Inject
    public Person(final Provider<SessionTask> sessionProvider, final Adapter adapter) {
        _sessionProvider = sessionProvider;
        _adapter = adapter;
    }

    public void start(){
        _sessionTask = _sessionProvider.get();
        _adapter.subscribe(_sessionTask);
    }

    public void stop(){
        _adapter.unsubscribe(_sessionTask);
    }

    @Override
    public void submit(final Command command) {
        _adapter.submit(command);
    }

    @Override
    public void subscribe(final AdapterEventListener listener) {
        _adapter.subscribe(listener);
    }

    @Override
    public void unsubscribe(final AdapterEventListener listener) {
        _adapter.unsubscribe(listener);
    }
}
