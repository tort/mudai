package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Location;
import com.tort.mudai.mapper.Mapper;

import java.util.List;

public class TravelTask extends StatedTask {
    private List<Direction> _path;
    private Command _command;

    @Inject
    TravelTask(@Assisted final Location to, final Mapper mapper) {
        _path = mapper.pathTo(to);
        if (_path == null) {
            System.out.println("NO PATH FOUND");
            fail();
        } else {
            if (!_path.isEmpty()) {
                goNext(_path.get(0));
            } else {
                System.out.println("EMPTY PATH");
                succeed();
            }
        }
    }

    private void goNext(final Direction direction) {
        _command = new SimpleCommand(direction.getName());
    }

    @Override
    public void move(String direction) {
        //TODO check current location has same title as planned, abort task otherwise
        if (_path.isEmpty()) {
            succeed();
            return;
        }

        _path.remove(0);

        if (!_path.isEmpty()) {
            goNext(_path.get(0));
        } else {
            succeed();
        }
    }

    @Override
    public Command pulse() {
        if(isTerminated()){
            return null;
        }

        final Command command = _command;
        _command = null;

        return command;
    }
}
