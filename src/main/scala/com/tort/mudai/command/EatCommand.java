package com.tort.mudai.command;

public class EatCommand implements RenderableCommand {
    private final String _food;

    public EatCommand(String food) {
        _food = food;
    }

    @Override
    public String render() {
        return "есть " + _food;
    }
}
