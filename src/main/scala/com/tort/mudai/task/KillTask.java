package com.tort.mudai.task;

import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.command.KillCommand;import com.tort.mudai.mapper.Mob;

public class KillTask extends StatedTask {
    private RenderableCommand _command;private final Mob _mob;
    private final TaskTerminateCallback _callback;

    public KillTask(Mob mob, TaskTerminateCallback callback) {
        _mob = mob;
        _callback = callback;
        _command = new KillCommand(_mob.name());
    }

    @Override
    public RenderableCommand pulse() {
        final RenderableCommand command = _command;
        _command = null;

        return command;
    }

    @Override
    public void kill(String target) {
        if(_mob.name().equals(target)){
            _callback.succeeded();
        }
    }
}
