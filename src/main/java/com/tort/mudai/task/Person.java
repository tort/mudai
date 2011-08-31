package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.PulseDistributor;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Location;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Person extends StatedTask {
    private final Provider<SessionTask> _sessionProvider;
    private final Provider<AbstractTask> _mapperTaskProvider;
    private final Provider<ProvisionTask> _provisionTaskProvider;
    private final CommandExecutor _commandExecutor;
    private final TravelTaskFactory _travelTaskFactory;

    private ScheduledExecutorService _pulseExecutor;

    private final EventDistributor _eventDistributor;
    private PulseDistributor _pulseDistributor;
    private final Provider<RoamingTask> _roamingTaskProvider;

    @Inject
    private Person(final Provider<SessionTask> sessionProvider,
                   @Named("mapperTask") final Provider<AbstractTask> mapperTask,
                   final ScheduledExecutorService executor,
                   final CommandExecutor commandExecutor,
                   final EventDistributor eventDistributor,
                   final Provider<ProvisionTask> provisionTaskProvider,
                   final TravelTaskFactory travelTaskFactory,
                   final Provider<RoamingTask> roamingTaskProvider,
                   final PulseDistributor pulseDistributor) {

        _sessionProvider = sessionProvider;
        _commandExecutor = commandExecutor;
        _mapperTaskProvider = mapperTask;
        _pulseExecutor = executor;
        _eventDistributor = eventDistributor;
        _provisionTaskProvider = provisionTaskProvider;
        _travelTaskFactory = travelTaskFactory;
        _pulseDistributor = pulseDistributor;
        _roamingTaskProvider = roamingTaskProvider;
        run();
    }

    public void subscribe(AbstractTask task) {
        _eventDistributor.subscribe(task);
    }

    public void start() {
        final SessionTask sessionTask = _sessionProvider.get();
        final AbstractTask mapperTask = _mapperTaskProvider.get();
        final ProvisionTask provisionTask = _provisionTaskProvider.get();
        final RoamingTask roamingTask = _roamingTaskProvider.get();

        _eventDistributor.subscribe(sessionTask);
        _eventDistributor.subscribe(mapperTask);
        _eventDistributor.subscribe(provisionTask);
        _eventDistributor.subscribe(roamingTask);

        _pulseDistributor.subscribe(sessionTask);
        _pulseDistributor.subscribe(mapperTask);
        _pulseDistributor.subscribe(provisionTask);
        _pulseDistributor.subscribe(roamingTask);

        _pulseExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    final Command pulse = pulse();
                    if (pulse != null) {
                        _commandExecutor.submit(pulse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void travel(final Location to) {
        final TravelTask travelTask = _travelTaskFactory.create(to, new TravelTaskTerminateCallback());
        _eventDistributor.subscribe(travelTask);
        _pulseDistributor.subscribe(travelTask);
    }

    @Override
    public Command pulse() {
        final Command command = _pulseDistributor.pulse();

        for (Task task : _eventDistributor.getTargets()) {
            if (task.isTerminated()) {
                _eventDistributor.unsubscribe(task);
            }
        }

        return command;
    }

    private static class TravelTaskTerminateCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {

        }

        @Override
        public void failed() {

        }
    }
}
