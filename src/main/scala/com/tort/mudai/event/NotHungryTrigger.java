package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Pattern;

public class NotHungryTrigger implements EventTrigger {
    private static final Pattern PATTERN = PatternUtil.compile(".*(?:Вы наелись\\.|Вы слишком сыты для этого \\!).*");
    private final EventDistributor _eventDistributor;

    public NotHungryTrigger(EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public Event fireEvent(String text) {
        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(AbstractTask task) {
                task.feelNotHungry();
            }
        });

        return null;
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
