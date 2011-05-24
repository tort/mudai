package com.tort.mudai.task;

import com.google.inject.Inject;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.SimpleCommand;
import com.tort.mudai.mapper.Location;
import com.tort.mudai.mapper.Mapper;

public class ProvisionTask extends AbstractTask {
    private final Mapper _mapper;
    private Command _command;

    @Inject
    public ProvisionTask(final Mapper mapper) {
        _mapper = mapper;
    }

    @Override
    public void move(final String direction) {
        final Location location = _mapper.currentLocation();

        String waterSource = location.getWaterSource();
        if(waterSource != null){
            _command = new SimpleCommand("пить " + waterSource);
        }
    }

    @Override
    public Command pulse() {
        final Command command = _command;
        _command = null;

        return command;
    }
}
