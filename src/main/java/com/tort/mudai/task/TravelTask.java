package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;
import com.tort.mudai.mapper.MapperException;

import java.util.List;

public class TravelTask extends StatedTask {
    private List<Direction> _path;
    private Command _command;

    @Inject
    TravelTask(@Assisted final String to, final Mapper mapper) {
        try {
            _path = mapper.pathTo(to);

            goNext(_path.get(0));
        } catch (MapperException e) {
            System.out.println("UNABLE TO TRAVEL: " + e.getMessage());
            terminate();
        }
    }

    private void goNext(final Direction direction) {
        _command = new SimpleCommand(direction.getName());
    }

    @Override
    public void move(String direction) {
        //TODO check current location has same title as planned, abort task otherwise
        if (_path.isEmpty()) {
            terminate();
            return;
        }

        _path.remove(0);

        if (!_path.isEmpty()) {
            goNext(_path.get(0));
        }
    }

    @Override
    public Command pulse() {
        final Command command = _command;
        _command = null;
        
        return command;
    }
}
