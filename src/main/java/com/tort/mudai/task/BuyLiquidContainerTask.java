package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.mapper.Mapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BuyLiquidContainerTask extends StatedTask {
    private final Mapper _mapper;
    private Command _command;
    private List<Direction> _path;

    @Inject
    public BuyLiquidContainerTask(final Mapper mapper) {
        _mapper = mapper;
        run();
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
        return null;
    }
}
