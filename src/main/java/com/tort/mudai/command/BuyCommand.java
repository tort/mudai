package com.tort.mudai.command;

public class BuyCommand implements Command {
    private String _containerName;

    public BuyCommand(final String containerName) {
        _containerName = containerName;
    }

    @Override
    public String render() {
        return "купить " + _containerName;
    }
}
