package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Location;
import com.tort.mudai.mapper.Mapper;
import com.tort.mudai.mapper.Mob;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Person extends AbstractTask {
    private static final Command EMPTY_COMMAND = null;

    private final Provider<SessionTask> _sessionProvider;
    private final Provider<AbstractTask> _mapperTaskProvider;
    private final Provider<ProvisionTask> _provisionTask;
    private final CommandExecutor _commandExecutor;
    private ScheduledExecutorService _pulseExecutor;

    private Mapper _mapper;
    private final EventDistributor _eventDistributor;

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
        _eventDistributor.subscribe(_mapperTaskProvider.get());
        _eventDistributor.subscribe(_sessionProvider.get());
        _eventDistributor.subscribe(_provisionTask.get());

        _pulseExecutor.scheduleAtFixedRate(new Runnable(){
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
        _eventDistributor.subscribe(new TravelTask(to, _mapper));
    }

    @Override
    public Command pulse() {
        for (Task task : _eventDistributor.getTargets()) {
            Command command = task.pulse();
            if(command != EMPTY_COMMAND){
                _commandExecutor.submit(command);

                return command;
            }
        }

        return EMPTY_COMMAND;
    }

    public void markWaterSource(final String waterSource) {
        final Location location = _mapper.currentLocation();
        location.setWaterSource(waterSource);
    }
}
