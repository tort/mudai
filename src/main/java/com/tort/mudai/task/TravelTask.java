package com.tort.mudai.task;

import com.tort.mudai.Adapter;
import com.tort.mudai.AdapterEventListener;
import com.tort.mudai.Handler;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.MoveEvent;
import com.tort.mudai.mapper.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelTask implements AdapterEventListener {
    private Map<Class, Handler> _events = new HashMap<Class, Handler>();
    private final List<Direction> _path;
    private final Adapter _adapter;

    public TravelTask(final Adapter adapter, final List<Direction> path) {
        if (path == null)
            throw new IllegalArgumentException("path is null");

        _adapter = adapter;
        _path = path;

        _events.put(MoveEvent.class, new MoveEventHandler());

        goNext(_path.get(0));
    }

    private void goNext(final Direction direction) {
        _adapter.submit(new SimpleCommand(direction.getName()));
    }

    @Override
    public void handle(final Event event) {
        try {
            final Handler handler = _events.get(event.getClass());
            if (handler != null) {
                handler.handle(event);
            }
        } catch (InterruptedException e) {
            System.out.println("Error queueing command");
        }
    }

    private class MoveEventHandler implements Handler<MoveEvent> {
        @Override
        public void handle(final MoveEvent event) throws InterruptedException {
            if (_path.isEmpty()){
                _adapter.unsubscribe((AdapterEventListener) this);

                return;
            }

            _path.remove(0);

            if (!_path.isEmpty()) {
                final Direction command = _path.get(0);
                _adapter.submit(new SimpleCommand(command.getName()));
            }
        }
    }
}
