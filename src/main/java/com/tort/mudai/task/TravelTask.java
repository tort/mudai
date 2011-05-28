package com.tort.mudai.task;

import com.tort.mudai.CommandExecutor;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.util.List;

public class TravelTask extends AbstractTask {
    private final List<Direction> _path;
    private Command _command;
    private Status _status = Status.RUNNING;

    public TravelTask(String to, final Mapper mapper) {
        final List<Direction> path = mapper.pathTo(to);
        if (path == null)
            throw new IllegalArgumentException("path is null");

        _path = path;

        goNext(_path.get(0));
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

    private void terminate() {
        _status = Status.TERMINATED;
    }

    @Override
    public Command pulse() {
        final Command command = _command;
        _command = null;
        
        return command;
    }

    @Override
    public Status status() {
        return _status;
    }
}
