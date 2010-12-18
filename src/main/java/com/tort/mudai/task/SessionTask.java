package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.*;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.command.StartSessionCommand;
import com.tort.mudai.event.AdapterExceptionEvent;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.LoginPromptEvent;
import com.tort.mudai.event.PasswordPromptEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SessionTask implements AdapterEventListener {
    private final PersonProperties _properties;
    private Map<Class, Handler> _eventMap = new HashMap<Class, Handler>();
    private CommandExecutor _executor;

    @Inject
    public SessionTask(final PersonProperties properties, final CommandExecutor executor) {
        _properties = properties;
        _executor = executor;

        _eventMap.put(LoginPromptEvent.class, new LoginPromptHandler());
        _eventMap.put(PasswordPromptEvent.class, new PasswordPromptHandler());
        _eventMap.put(AdapterExceptionEvent.class, new AdapterExceptionHandler());

        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable(){
            @Override
            public void run() {
                System.out.println("CONNECTING");
                _executor.submit(new StartSessionCommand());
            }
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void handle(final Event event) {
        try {
            final Handler handler = _eventMap.get(event.getClass());
            if (handler != null){
                handler.handle(event);
            }
        } catch (InterruptedException e) {
            System.out.println("Error queueing command");
        }
    }

    public class LoginPromptHandler implements Handler {
        @Override
        public void handle(final Event e) throws InterruptedException {
            System.out.println(_properties.getLogin());
            _executor.submit(new SimpleCommand(_properties.getLogin()));
        }
    }

    private class PasswordPromptHandler implements Handler {
        @Override
        public void handle(final Event e) throws InterruptedException {
            System.out.println(_properties.getPassword());
            _executor.submit(new SimpleCommand(_properties.getPassword()));
        }
    }

    private class AdapterExceptionHandler implements Handler {
        @Override
        public void handle(final Event e) throws InterruptedException {
            System.out.println("CONNECTION DROPPED");
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable(){
                @Override
                public void run() {
                    System.out.println("RECONNECTING");
                    _executor.submit(new StartSessionCommand());
                }
            }, 5, 10, TimeUnit.SECONDS);
        }
    }
}
