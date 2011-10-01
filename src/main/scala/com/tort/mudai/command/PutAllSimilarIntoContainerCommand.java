package com.tort.mudai.command;

public class PutAllSimilarIntoContainerCommand implements RenderableCommand {
    private final String _item;
    private final String _container;

    public PutAllSimilarIntoContainerCommand(String item, String container) {
        _item = item;
        _container = container;
    }

    @Override
    public String render() {
        return "положить все." + _item + " " + _container;
    }
}
