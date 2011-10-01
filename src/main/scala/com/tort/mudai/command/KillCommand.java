package com.tort.mudai.command;

public class KillCommand implements RenderableCommand {
    private final String _mobName;

    public KillCommand(final String mobName) {
        _mobName = mobName;
    }

    @Override
    public String render() {
        return "убить " + _mobName;
    }
}
