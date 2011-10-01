package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.command.MoveCommand;
import com.tort.mudai.command.OpenCommand;
import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.JMapperWrapper;
import com.tort.mudai.mapper.Location;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TravelTask extends StatedTask {
    private static final String DEFAULT_OBSTACLE = "дверь";
    private List<Direction> _path;
    private final TaskTerminateCallback _callback;
    private Queue<RenderableCommand> _commands = new LinkedList<RenderableCommand>();

    @Inject
    public TravelTask(@Assisted final Location to, @Assisted TaskTerminateCallback callback, final JMapperWrapper mapper) {
        _callback = callback;
        _path = mapper.pathTo(to);
        if (_path == null) {
            System.out.println("NO PATH FOUND");
            fail();
            _callback.failed();
        } else {
            if (!_path.isEmpty()) {
                System.out.println("PATH LENGTH: " + _path.size());
                goNext(_path.get(0));
            } else {
                System.out.println("EMPTY PATH");
                succeed();
                _callback.succeeded();
            }
        }
    }

    private void goNext(final Direction direction) {
        _commands.add(new SimpleCommand(direction.getName()));
    }

    @Override
    public void glance(String direction, RoomSnapshot roomSnapshot) {
        //TODO check current currentLocation has same title as planned, abort task otherwise
        if (_path.isEmpty()) {
            succeed();
            _callback.succeeded();
            return;
        }

        _path.remove(0);

        if (!_path.isEmpty()) {
            goNext(_path.get(0));
        } else {
            succeed();
            _callback.succeeded();
        }
    }

    @Override
    public void discoverObstacle(String obstacle) {
        _commands.add(new OpenCommand(_path.get(0), obstacle != null ? obstacle : DEFAULT_OBSTACLE));
        _commands.add(new MoveCommand(_path.get(0)));
    }

    @Override
    public RenderableCommand pulse() {
        if(isTerminated()){
            return null;
        }

        return _commands.poll();
    }
}
