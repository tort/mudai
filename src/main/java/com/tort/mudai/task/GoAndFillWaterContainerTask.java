package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GoAndFillWaterContainerTask extends StatedTask {
    private final Mapper _mapper;
    private Command _command;
    private List<Direction> _path;
    private final static int THIRST_INTERVAL = 12;
    private final static int HUNGER_INTERVAL = 21;

    @Inject
    public GoAndFillWaterContainerTask(final Mapper mapper) {
        _mapper = mapper;
    }

    @Override
    public void move(final String direction) {
    }

    @Override
    public void feelThirst() {
        System.out.println("FEEL THIRST: " + new SimpleDateFormat().format(new Date()));
    }

    @Override
    public void feelHunger(){
        System.out.println("FEEL HUNGER: " + new SimpleDateFormat().format(new Date()));
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
