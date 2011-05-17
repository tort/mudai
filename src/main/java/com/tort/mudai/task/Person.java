package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.command.Command;
import com.tort.mudai.event.Event;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Person implements Task {
    private final Provider<SessionTask> _sessionProvider;
    private final CommandExecutor _commandExecutor;
    private ScheduledExecutorService _executor;

    private SessionTask _sessionTask;
    private Mapper _mapper;
    private List<Task> _tasks = new ArrayList<Task>();
    private static final Command EMPTY_COMMAND = null;

    @Inject
    protected Person(final Provider<SessionTask> sessionProvider, final CommandExecutor commandExecutor, final Mapper mapper, final ScheduledExecutorService executor) {
        _sessionProvider = sessionProvider;
        _commandExecutor = commandExecutor;
        _mapper = mapper;
        _executor = executor;
    }

    public void subscribe(Task task) {
        _tasks.add(task);
    }

    public void start() {
        _sessionTask = _sessionProvider.get();

        _tasks.add(_mapper);
        _tasks.add(_sessionTask);

        _executor.scheduleAtFixedRate(new Runnable(){
            @Override
            public void run() {
                pulse();
            }
        }, 1, 1, TimeUnit.SECONDS);
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
        _tasks.add(new TravelTask(_commandExecutor, to, _mapper));
    }

    @Override
    public void handle(final Event e) {
        for (Task task : _tasks) {
            task.handle(e);
        }
    }

    @Override
    public Command pulse() {
        for (Task task : _tasks) {
            Command command = task.pulse();
            if(command != EMPTY_COMMAND){
                _commandExecutor.submit(command);

                return command;
            }
        }

        return EMPTY_COMMAND;
    }
}
