package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Person extends StatedTask {
    private static final Command EMPTY_COMMAND = null;

    private final Provider<SessionTask> _sessionProvider;
    private final Provider<AbstractTask> _mapperTaskProvider;
    private final Provider<ProvisionTask> _provisionTask;
    private final CommandExecutor _commandExecutor;
    private ScheduledExecutorService _pulseExecutor;

    private Mapper _mapper;
    private final EventDistributor _eventDistributor;
    private final List<Task> _pulseTargets = new ArrayList();

    @Inject
    private Person(final Provider<SessionTask> sessionProvider,
                   @Named("mapperTask") final Provider<AbstractTask> mapperTask,
                   final Mapper mapper,
                   final ScheduledExecutorService executor,
                   final CommandExecutor commandExecutor,
                   final EventDistributor eventDistributor,
                   final Provider<ProvisionTask> provisionTask) {

        _sessionProvider = sessionProvider;
        _commandExecutor = commandExecutor;
        _mapper = mapper;
        _mapperTaskProvider = mapperTask;
        _pulseExecutor = executor;
        _eventDistributor = eventDistributor;
        _provisionTask = provisionTask;
    }

    public void subscribe(AbstractTask task) {
        _eventDistributor.subscribe(task);
    }

    public void start() {
        final SessionTask sessionTask = _sessionProvider.get();
        final AbstractTask mapperTask = _mapperTaskProvider.get();
        final ProvisionTask provisionTask = _provisionTask.get();

        _eventDistributor.subscribe(sessionTask);
        _eventDistributor.subscribe(mapperTask);
        _eventDistributor.subscribe(provisionTask);

        _pulseTargets.add(sessionTask);
        _pulseTargets.add(mapperTask);
        _pulseTargets.add(provisionTask);

        _pulseExecutor.scheduleAtFixedRate(new Runnable() {
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

    public void travel(final String to) {
        _eventDistributor.subscribe(new TravelTask(to, _mapper));
    }

    @Override
    public Command pulse() {
        for (Task task : _pulseTargets) {
            Command command = task.pulse();
            if (command != EMPTY_COMMAND) {
                _commandExecutor.submit(command);

                return command;
            }
            
            if(task.isInitializing())
                break;
        }

        for (Task task : _eventDistributor.getTargets()) {
            if (task.isTerminated()) {
                _eventDistributor.unsubscribe(task);
            }
        }

        return EMPTY_COMMAND;
    }

    @Override
    public Status status() {
        return Status.RUNNING;
    }

    public void markWaterSource(final String waterSource) {
        _mapper.markWaterSource(waterSource);
    }
}
