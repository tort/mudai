package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.Adapter;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.event.Event;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

public class Person implements Task {
    private final Provider<SessionTask> _sessionProvider;
    private final CommandExecutor _adapter;

    private SessionTask _sessionTask;
    private Mapper _mapper;
    private List<Task> _tasks = new ArrayList<Task>();

    @Inject
    protected Person(final Provider<SessionTask> sessionProvider, final Adapter adapter, final Mapper mapper) {
        _sessionProvider = sessionProvider;
        _adapter = adapter;
        _mapper = mapper;
    }

    public void subscribe(Task task){
        _tasks.add(task);
    }

    public void start(){
        _sessionTask = _sessionProvider.get();

        _tasks.add(_mapper);
        _tasks.add(_sessionTask);
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
        _tasks.add(new TravelTask(_adapter, to, _mapper));
    }

    @Override
    public void handle(final Event e) {
        for (Task task : _tasks) {
            task.handle(e);
        }
    }
}
