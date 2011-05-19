package com.tort.mudai.task;

import com.tort.mudai.Handler;
import com.tort.mudai.event.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EventDistributor {
    private Map<Class, Handler> _events = new HashMap<Class, Handler>();

    public EventDistributor() {
        _events.put(MoveEvent.class, new MoveEventHandler());
        _events.put(AdapterExceptionEvent.class, new AdapterExceptionEventHandler());
        _events.put(ConnectionClosedEvent.class, new ConnectionClosedEventHandler());
        _events.put(RawReadEvent.class, new RawReadEventHandler());
        _events.put(ProgrammerErrorEvent.class, new ProgrammerErrorEventHandler());
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

    private class AdapterExceptionEventHandler implements Handler<AdapterExceptionEvent> {
        @Override
        public void handle(Task task, AdapterExceptionEvent event) {
            task.adapterException(event.getException());
        }
    }

    private class ConnectionClosedEventHandler implements Handler {
        @Override
        public void handle(Task task, Event event) {
            task.connectionClosed();
        }
    }

    private class RawReadEventHandler implements Handler<RawReadEvent> {
        @Override
        public void handle(Task task, RawReadEvent event) {
            task.rawRead(event.getInCharBuffer());
        }
    }

    private class ProgrammerErrorEventHandler implements Handler<ProgrammerErrorEvent> {
        @Override
        public void handle(Task task, ProgrammerErrorEvent event) {
            task.programmerError(event.getException());
        }
    }
}
