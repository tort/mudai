package com.tort.mudai.command;

import com.tort.mudai.AdapterEventListener;
import com.tort.mudai.task.TravelTask;

public class UnsubscribeCommand implements Command {
    private final TravelTask _travelTask;

    public UnsubscribeCommand(final TravelTask travelTask) {
        _travelTask = travelTask;
    }

    @Override
    public String render() {
        throw new IllegalArgumentException("shouldn't ever be called");
    }

    public AdapterEventListener getTask() {
        return _travelTask;
    }
}
