package com.tort.mudai;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.LoginPromptEvent;
import com.tort.mudai.event.PasswordPromptEvent;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Person implements AdapterEventListener {
    private final BlockingQueue<Command> _commands;
    private final PersonProperties _properties;
    private Map<Class, Handler> _eventMap = new HashMap<Class, Handler>();
    private final Handler _loginPromptHandler = new LoginPromptHandler();
    private final Handler _passwordPromptHandler = new PasswordPromptHandler();

    @Inject
    public Person(final BlockingQueue commands, PersonProperties properties) {
        _commands = commands;
        _properties = properties;

        _eventMap.put(LoginPromptEvent.class, _loginPromptHandler);
        _eventMap.put(PasswordPromptEvent.class, _passwordPromptHandler);
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
            _commands.put(new SimpleCommand(_properties.getLogin()));
        }
    }

    private class PasswordPromptHandler implements Handler {
        @Override
        public void handle(final Event e) throws InterruptedException {
            System.out.println(_properties.getPassword());
            _commands.put(new SimpleCommand(_properties.getPassword()));
        }
    }
}
