package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.tort.mudai.Adapter;
import com.tort.mudai.AdapterEventListener;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.command.Command;
import com.tort.mudai.event.Event;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.util.List;

public class Person implements CommandExecutor, AdapterEventListener {
    private final Provider<SessionTask> _sessionProvider;
    private final Provider<Task> _mapperTaskProvider;
    private final CommandExecutor _adapter;

    private Mapper _mapper;
    private final EventDistributor _eventDistributor = new EventDistributor();

    @Inject
    protected Person(final Provider<SessionTask> sessionProvider,
                     @Named("mapperTask") final Provider<Task> mapperTask,
                     final Adapter adapter,
                     final Mapper mapper) {

        _sessionProvider = sessionProvider;
        _adapter = adapter;
        _mapper = mapper;
        _mapperTaskProvider = mapperTask;
    }

    public void subscribe(Task task) {
        _eventDistributor.subscribe(task);
    }

    public void start() {
        _eventDistributor.subscribe(_mapperTaskProvider.get());
        _eventDistributor.subscribe(_sessionProvider.get());
    }

    @Override
    public void submit(final Command command) {
        _adapter.submit(command);
    }

    public String pathTo(final String location) {
        final List<Direction> directions = _mapper.pathTo(location);

        StringBuilder result = new StringBuilder();
        for (Direction direction : directions) {
            result.append(direction.getName());
        }

        return result.toString();
    }

    public List<String> locationTitles() {
        return _mapper.knownLocations();
    }

    public void travel(final String to) {
        _eventDistributor.subscribe(new TravelTask(_adapter, to, _mapper));
    }

    @Override
    public void handle(final Event e) {
        _eventDistributor.invoke(e);
    }

}
