package com.tort.mudai.command;

public class MultiCommand implements RenderableCommand {
    private final RenderableCommand[] _commands;

    public MultiCommand(RenderableCommand[] commands) {
        _commands = commands;
    }

    @Override
    public String render() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RenderableCommand[] getCommands() {
        return _commands;
    }
}
