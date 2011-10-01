package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Pattern;

public class NotThirstyTrigger implements EventTrigger {
    private final EventDistributor _eventDistributor;
    private static final Pattern PATTERN = PatternUtil.compile(".*Вы не чувствуете жажды\\..*");

    public NotThirstyTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public Event fireEvent(final String text) {
        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(final AbstractTask task) {
                task.feelNotThirsty();
            }
        });

        return null;
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN.matcher(text).matches();
    }
}
