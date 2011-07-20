package com.tort.mudai.command;

public class TakeItemFromContainer implements Command {
    private final String _item;
    private final String _container;

    public TakeItemFromContainer(String item, String container) {
        _item = item;
        _container = container;
    }

    @Override
    public String render() {
        return "взять " + _item + " " + _container;
    }
}
