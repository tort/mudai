package com.tort.mudai.task;

import com.tort.mudai.CommandExecutor;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.util.List;

public class TravelTask extends AbstractTask {
    private final List<Direction> _path;
    private final String _to;
    private final Mapper _mapper;

    public TravelTask(String to, final Mapper mapper) {
        if (mapper == null)
            throw new IllegalArgumentException("mapper is null");

        if (to == null)
            throw new IllegalArgumentException("to is null");

        final List<Direction> path = mapper.pathTo(to);
        if (path == null)
            throw new IllegalArgumentException("path is null");

        _to = to;
        _path = path;
        _mapper = mapper;

        goNext(_path.get(0));
    }

    private void goNext(final Direction direction) {
    }

    @Override
    public void move(String direction) {
        //TODO check current location has same title as planned, abort task otherwise
        if (_path.isEmpty()) {
            return;
        }

        _path.remove(0);

        if (!_path.isEmpty()) {
            goNext(_path.get(0));
        }
    }

    @Override
    public Command pulse() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
