package com.tort.mudai.command;

public class DrinkCommand implements Command {
    private final String _liquidContainer;

    public DrinkCommand(final String liquidContainer) {
        _liquidContainer = liquidContainer;
    }

    @Override
    public String render() {
        return "пить " + _liquidContainer;
    }

}
