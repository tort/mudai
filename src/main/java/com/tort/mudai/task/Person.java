package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.Adapter;
import com.tort.mudai.AdapterEventListener;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.Handler;
import com.tort.mudai.command.Command;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.MoveEvent;
import com.tort.mudai.mapper.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Person implements CommandExecutor, AdapterEventListener {
    private final Provider<SessionTask> _sessionProvider;
    private final CommandExecutor _adapter;

    private SessionTask _sessionTask;
    private Mapper _mapper;
    private List<Task> _tasks = new ArrayList<Task>();
    private final EventDistributor eventDistributor = new EventDistributor();

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
        _tasks.add(new TravelTask(_adapter, to, _mapper));
    }

    @Override
    public void handle(final Event e) {
        eventDistributor.invoke(e, _tasks);
    }

    private class EventDistributor {
        private Map<Class, Handler> _events = new HashMap<Class, Handler>();

        public EventDistributor(){
            _events.put(MoveEvent.class, new MoveEventHandler());
        }

        public void invoke(Event e, List<Task> tasks) {
            Handler handler = _events.get(e);
            for (Task task : tasks) {
                handler.handle(task, e);
            }
        }

        private class MoveEventHandler implements Handler<MoveEvent> {
            @Override
            public void handle(Task task, MoveEvent event) {
                task.move(event.getDirection());
            }
        }
    }
}
