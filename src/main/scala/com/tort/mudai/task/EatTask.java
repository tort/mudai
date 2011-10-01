package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.command.*;
import com.tort.mudai.mapper.JMapperWrapper;
import com.tort.mudai.mapper.Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EatTask extends StatedTask {
    private final static int HUNGER_INTERVAL = 21;
    private static final RenderableCommand EMPTY_COMMAND = null;
    private static final String PREFERRED_FOOD = "хлеб";
    private static final String PREFERRED_CONTAINER = "мешок";

    private final TaskTerminateCallback _callback;

    private volatile boolean _feelHunger;
    private volatile Queue<RenderableCommand> _commandQueue = new LinkedList<RenderableCommand>();
    private GoAndDoTaskFactory _goAndDoTaskFactory;
    private JMapperWrapper _mapper;
    private final List<Task> _subtasks = new CopyOnWriteArrayList<Task>();
    private final EventDistributor _eventDistributor;

    @Inject
    public EatTask(ScheduledExecutorService executor,
                   GoAndDoTaskFactory goAndDoTaskFactory,
                   JMapperWrapper mapper,
                   EventDistributor eventDistributor,
                   @Assisted TaskTerminateCallback callback) {
        _callback = callback;
        _goAndDoTaskFactory = goAndDoTaskFactory;
        _mapper = mapper;
        _eventDistributor = eventDistributor;

        final Runnable drinkTask = new Runnable() {
            @Override
            public synchronized void run() {
                _feelHunger = true;
                _commandQueue.add(new TakeItemFromContainer(PREFERRED_FOOD, PREFERRED_CONTAINER));
                _commandQueue.add(new EatCommand(PREFERRED_FOOD));
            }
        };
        executor.scheduleAtFixedRate(drinkTask, 0, HUNGER_INTERVAL, TimeUnit.MINUTES);
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

        RenderableCommand command = _commandQueue.poll();
        if (command instanceof EatCommand && !_feelHunger) {
            command = _commandQueue.poll();
        }
        return command;

    }

    @Override
    public void couldNotFindItem(String item) {
        if (item.equals(PREFERRED_CONTAINER)) {
            Location to = _mapper.nearestShop();
            if(to != null) {
                final GoAndDoTask buyContainerTask = _goAndDoTaskFactory.create(to, new BuyCommand(PREFERRED_CONTAINER), new BuyContainerTaskTerminateCallback());
                _eventDistributor.subscribe(buyContainerTask);
                _subtasks.add(buyContainerTask);
            } else {
                System.out.println("NO SHOPS EXIST");
                fail();
                _callback.failed();
            }
        }
    }

    @Override
    public void couldNotFindItemInContainer(final String item, String container) {
        if (item.equals(PREFERRED_FOOD)) {
            Location to = _mapper.nearestTavern();
            if(to != null) {
                final GoAndDoTask buyFoodTask = _goAndDoTaskFactory.create(to, new BuyCommand(PREFERRED_FOOD, 10), new BuyFoodTaskTerminateCallback(item));
                _eventDistributor.subscribe(buyFoodTask);
                _subtasks.add(buyFoodTask);
            } else {
                System.out.println("NO TAVERNS EXIST");
                fail();
                _callback.failed();
            }
        }
    }

    @Override
    public void eat(String food) {
        synchronized (this) {
            if (_feelHunger) {
                _commandQueue.add(new TakeItemFromContainer(PREFERRED_FOOD, PREFERRED_CONTAINER));
                _commandQueue.add(new EatCommand(PREFERRED_FOOD));
            }
        }
    }

    @Override
    public void feelNotHungry() {
        synchronized (this) {
            _feelHunger = false;
            _commandQueue.add(new PutAllSimilarIntoContainerCommand(PREFERRED_FOOD, PREFERRED_CONTAINER));

            if (isInitializing())
                run();
        }
    }

    private class BuyFoodTaskTerminateCallback implements TaskTerminateCallback {
        private final String _item;

        public BuyFoodTaskTerminateCallback(String item) {
            _item = item;
        }

        @Override
        public void succeeded() {
            _commandQueue.add(new PutAllSimilarIntoContainerCommand(_item, PREFERRED_CONTAINER));
            _commandQueue.add(new TakeItemFromContainer(PREFERRED_FOOD, PREFERRED_CONTAINER));
            _commandQueue.add(new EatCommand(PREFERRED_FOOD));
        }

        @Override
        public void failed() {
            fail();
            _callback.failed();
        }
    }

    private class BuyContainerTaskTerminateCallback implements TaskTerminateCallback {
        @Override
        public void succeeded() {
            _commandQueue.add(new TakeItemFromContainer(PREFERRED_FOOD, PREFERRED_CONTAINER));
        }

        @Override
        public void failed() {
            fail();
            _callback.failed();
        }
    }
}
