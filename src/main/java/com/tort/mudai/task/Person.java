package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.tort.mudai.Adapter;
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
    private final Provider<Task> _mapperTaskProvider;
    private ScheduledExecutorService _executor;

    private Mapper _mapper;
    private final EventDistributor _eventDistributor = new EventDistributor();

    @Inject
    protected Person(final Provider<SessionTask> sessionProvider,
                     @Named("mapperTask") final Provider<Task> mapperTask,
                     final Adapter adapter,
                     final Mapper mapper) {

    private List<Task> _tasks = new ArrayList<Task>();
    private static final Command EMPTY_COMMAND = null;

        _sessionProvider = sessionProvider;
        _commandExecutor = commandExecutor;
        _mapper = mapper;
        _mapperTaskProvider = mapperTask;
        _executor = executor;
    }

    public void subscribe(Task task) {
        _eventDistributor.subscribe(task);
    }

    public void start() {
        _eventDistributor.subscribe(_mapperTaskProvider.get());
        _eventDistributor.subscribe(_sessionProvider.get());

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
        _eventDistributor.subscribe(new TravelTask(_adapter, to, _mapper));
    }

    @Override
    public void handle(final Event e) {
        _eventDistributor.invoke(e);
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
