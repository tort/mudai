package com.tort.mudai.command;

import com.tort.mudai.mapper.Direction;

public class MoveCommand implements RenderableCommand {
    private Direction _direction;

    public MoveCommand(final Direction direction) {
        _direction = direction;
    }

    @Override
    public String render() {
        return _direction.id();
    }
}
