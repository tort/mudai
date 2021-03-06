package com.tort.mudai.command;

public class SimpleCommand implements RenderableCommand {
    private final String _name;

    public SimpleCommand(final String name) {
        _name = name;
    }

    @Override
    public String render() {
        return _name;
    }
}
