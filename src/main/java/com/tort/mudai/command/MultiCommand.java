package com.tort.mudai.command;

public class MultiCommand implements Command {
    private final Command[] _commands;

    public MultiCommand(Command[] commands) {
        _commands = commands;
    }

    @Override
    public String render() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Command[] getCommands() {
        return _commands;
    }
}
