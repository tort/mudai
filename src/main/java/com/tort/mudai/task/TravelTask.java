package com.tort.mudai.task;

import com.tort.mudai.Adapter;
import com.tort.mudai.AdapterEventListener;
import com.tort.mudai.Handler;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.command.UnsubscribeCommand;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.MoveEvent;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelTask implements AdapterEventListener {
    private Map<Class, Handler> _events = new HashMap<Class, Handler>();
    private final List<Direction> _path;
    private final Adapter _adapter;
    private final String _to;
    private final Mapper _mapper;

    public TravelTask(final Adapter adapter, final String to, final Mapper mapper) {
        if(mapper == null)
            throw new IllegalArgumentException("mapper is null");

        if(to == null)
            throw new IllegalArgumentException("to is null");

        final List<Direction> path = mapper.pathTo(to);
        if (path == null)
            throw new IllegalArgumentException("path is null");

        _to = to;
        _path = path;
        _adapter = adapter;
        _mapper = mapper;

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
            //TODO check current location has same title as planned, abort task otherwise
            if (_path.isEmpty()){
                _adapter.submit(new UnsubscribeCommand(TravelTask.this));

                return;
            }

            _path.remove(0);

            if (!_path.isEmpty()) {
                goNext(_path.get(0));
            }
        }
    }
}
