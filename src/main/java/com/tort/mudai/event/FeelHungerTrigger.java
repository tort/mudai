package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Pattern;

public class FeelHungerTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile(".*^Вы голодны\\.$.*");
    private EventDistributor _eventDistributor;

    public FeelHungerTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public Event fireEvent(final String text) {
        _eventDistributor.invoke(new Handler() {
            @Override
            public void handle(final AbstractTask task) {
                task.feelHunger();
            }
        });

        return null;
    }

    @Override
    public boolean matches(final String text) {
        return _pattern.matcher(text).matches();
    }
}
