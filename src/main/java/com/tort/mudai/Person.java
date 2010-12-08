package com.tort.mudai;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.LoginCommand;
import com.tort.mudai.event.Event;
import com.tort.mudai.event.LoginPromptEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Person implements AdapterEventListener {
    private static final String NAME = "ладень";

    private BlockingQueue<Command> _commands;
    private Map<Class, Handler> _eventMap = new HashMap<Class, Handler>();
    private final Handler loginPromptHandler = new LoginPromptHandler();

    @Inject
    public Person(BlockingQueue commands){
        _commands = commands;

        _eventMap.put(LoginPromptEvent.class, loginPromptHandler);
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
            System.out.println(NAME);
            _commands.put(new LoginCommand(NAME));
        }
    }
}
