package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.util.List;

public class ProvisionTask extends AbstractTask {
    private final Mapper _mapper;
    private Command _command;
    private List<Direction> _path;

    @Inject
    public ProvisionTask(final Mapper mapper) {
        _mapper = mapper;
    }

    @Override
    public void move(final String direction) {
        goNext();
    }

    @Override
    public void feelThirst() {
        if (_path == null) {
            _path = _mapper.pathToNearestWaterSource();
            goNext();
        }
    }

    private void goNext() {
        if(_path.isEmpty()){
            _command = new SimpleCommand("пить " + _mapper.currentLocation().getWaterSource());
            return;
        }

        final String direction = _path.get(0).getName();
        _path.remove(0);

        _command = new SimpleCommand(direction);
    }

    @Override
    public Command pulse() {
        final Command command = _command;
        _command = null;

        return command;
    }

    @Override
    public Status status() {
        return Status.RUNNING;
    }
}
