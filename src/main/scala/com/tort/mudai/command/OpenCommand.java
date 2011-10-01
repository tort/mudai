package com.tort.mudai.command;

import com.tort.mudai.mapper.Direction;

public class OpenCommand implements RenderableCommand {
    private final Direction _direction;
    private final String _obstacle;

    public OpenCommand(Direction direction, String obstacle) {
        _direction = direction;
        _obstacle = obstacle;
    }

    @Override
    public String render() {
        return "открыть " + _obstacle + " " + _direction.getName();
    }
}
