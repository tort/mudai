package com.tort.mudai.task;

import com.tort.mudai.Handler;
import com.tort.mudai.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EventDistributor {
    private Map<Class, Handler> _events = new HashMap<Class, Handler>();
    private List<Task> _tasks = new ArrayList<Task>();

    public EventDistributor() {
        _events.put(MoveEvent.class, new MoveEventHandler());
        _events.put(AdapterExceptionEvent.class, new AdapterExceptionEventHandler());
        _events.put(ConnectionClosedEvent.class, new ConnectionClosedEventHandler());
        _events.put(RawReadEvent.class, new RawReadEventHandler());
        _events.put(ProgrammerErrorEvent.class, new ProgrammerErrorEventHandler());
        _events.put(LoginPromptEvent.class, new LoginPromptEventHandler());
        _events.put(PasswordPromptEvent.class, new PasswordPromptEventHandler());
    }

    public void invoke(Event e) {
        Handler handler = _events.get(e);
        for (Task task : _tasks) {
            handler.handle(task, e);
        }
    }

    public void subscribe(Task task) {
        _tasks.add(task);
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

    private class LoginPromptEventHandler implements Handler<LoginPromptEvent> {
        @Override
        public void handle(Task task, LoginPromptEvent event) {
            task.loginPrompt();
        }
    }

    private class PasswordPromptEventHandler implements Handler<PasswordPromptEvent> {
        @Override
        public void handle(Task task, PasswordPromptEvent event) {
            task.passwordPrompt();
        }
    }
}
