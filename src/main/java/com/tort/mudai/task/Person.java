package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.Adapter;
import com.tort.mudai.AdapterEventListener;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.EventSource;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Location;
import com.tort.mudai.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

public class Person implements CommandExecutor, EventSource {
    private final Provider<SessionTask> _sessionProvider;
    private final Adapter _adapter;

    private SessionTask _sessionTask;
    private Mapper _mapper;

    @Inject
    public Person(final Provider<SessionTask> sessionProvider, final Adapter adapter, final Mapper mapper) {
        _sessionProvider = sessionProvider;
        _adapter = adapter;
        _mapper = mapper;
    }

    public void start(){
        _sessionTask = _sessionProvider.get();
        _adapter.subscribe(_sessionTask);
        _adapter.subscribe(_mapper);
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

    public String getPathTo(final String location) {
        return _mapper.getPathTo(location);
    }

    public List<String> locationTitles() {
        return _mapper.knownLocations();
    }
}
