package com.tort.mudai.command;

public class LoginCommand implements Command {
    private final String _name;

    public LoginCommand(final String name) {
        _name = name;
    }

    @Override
    public String render() {
        return _name;
    }
}
