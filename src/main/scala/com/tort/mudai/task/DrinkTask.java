package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.command.DrinkCommand;
import com.tort.mudai.command.FillLiquidContainerCommand;
import com.tort.mudai.event.LiquidContainer;
import com.tort.mudai.mapper.JMapperWrapper;
import com.tort.mudai.mapper.Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DrinkTask extends StatedTask {
    private final static int THIRST_INTERVAL = 12;
    private static final RenderableCommand EMPTY_COMMAND = null;

    private final PersonProperties _personProperties;
    private final EventDistributor _eventDistributor;
    private final ScheduledExecutorService _executor;
    private final JMapperWrapper _mapper;
    private final TaskTerminateCallback _callback;
    private final List<Task> _subtasks = new CopyOnWriteArrayList<Task>();

    private volatile boolean _feelThirst = false;
    private volatile Queue<RenderableCommand> _commands = new LinkedList<RenderableCommand>();
    private final TravelTaskFactory _goAndDoTaskFactory;

    @Inject
    public DrinkTask(final EventDistributor eventDistributor,
                     final ScheduledExecutorService executor,
                     final JMapperWrapper mapper,
                     final TravelTaskFactory goAndDoTaskFactory,
                     final PersonProperties personProperties,
                     @Assisted TaskTerminateCallback callback) {
        _eventDistributor = eventDistributor;
        _personProperties = personProperties;
        _executor = executor;
        _mapper = mapper;
        _goAndDoTaskFactory = goAndDoTaskFactory;
        _callback = callback;

        final Runnable drinkTask = new Runnable() {
            @Override
            public synchronized void run() {
                _feelThirst = true;

                _commands.add(new DrinkCommand(personProperties.getLiquidContainer()));
            }
        };
        _executor.scheduleAtFixedRate(drinkTask, 0, THIRST_INTERVAL, TimeUnit.MINUTES);
    }

    @Override
    public synchronized RenderableCommand pulse() {
        if (isTerminated())
            return null;

        for (Task task : _subtasks) {
            RenderableCommand command = task.pulse();
            if (command != EMPTY_COMMAND) {
                return command;
            }

            if (task.isInitializing())
                break;
        }

        List<Task> terminatedTasks = new ArrayList<Task>();
        for (Task task : _subtasks) {
            if (task.isTerminated()) {
                terminatedTasks.add(task);
            }
        }
        for (Task terminatedTask : terminatedTasks) {
            _subtasks.remove(terminatedTask);
        }

        return _commands.poll();
    }

    @Override
    public void feelNotThirsty() {
        _feelThirst = false;
    }

    @Override
    public void drink() {
        if (_feelThirst) {
            String container = _personProperties.getLiquidContainer();
            String waterSource = _mapper.currentLocation().waterSource().get();

            String from = container;
            if (waterSource != null)
                from = waterSource;

            _commands.add(new DrinkCommand(from));
        }
    }

    @Override
    public void examineWaterContainer(final LiquidContainer.State state) {
        if (state == LiquidContainer.State.EMPTY) {
            Location to = _mapper.nearestWaterSource();
            if (to != null) {
                final TravelTask fillContainerTask = _goAndDoTaskFactory.create(to, new TravelForContainerTerminateCallback());
                _subtasks.add(fillContainerTask);
                _eventDistributor.subscribe(fillContainerTask);
            } else {
                System.out.println("NO WATER SOURCES EXIST");
            }
        }
    }

    private class BuyContainerCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {
        }

        @Override
        public void failed() {
            fail();
        }
    }

    private class TravelForContainerTerminateCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {
            String container = _personProperties.getLiquidContainer();
            String waterSource = _mapper.currentLocation().waterSource().get();

            _commands.add(new FillLiquidContainerCommand(waterSource, container));
            _commands.add(new DrinkCommand(waterSource));
        }

        @Override
        public void failed() {
            fail();
        }
    }
}
