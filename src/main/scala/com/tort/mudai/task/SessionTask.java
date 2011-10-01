package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.CommandExecutor;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.command.StartSessionCommand;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SessionTask extends StatedTask {
    private final PersonProperties _properties;
    private CommandExecutor _executor;
    private RenderableCommand _command;

    @Inject
    public SessionTask(final PersonProperties properties, final CommandExecutor executor) {
        _properties = properties;
        _executor = executor;

        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable(){
            @Override
            public void run() {
                System.out.println("CONNECTING");
                _executor.submit(new StartSessionCommand());
            }
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void loginPrompt() {
        System.out.println(_properties.getLogin());
        _command = new SimpleCommand(_properties.getLogin());
    }

    @Override
    public void passwordPrompt() {
        System.out.println(_properties.getPassword());
        _command = new SimpleCommand(_properties.getPassword());

        run();
    }

    @Override
    public RenderableCommand pulse() {
        final RenderableCommand command = _command;
        _command = null;

        return command;
    }

    @Override
    public void adapterException(Exception e) {
        System.out.println("CONNECTION DROPPED");
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("RECONNECTING");
                _executor.submit(new StartSessionCommand());
            }
        }, 5, 10, TimeUnit.SECONDS);
    }
}
