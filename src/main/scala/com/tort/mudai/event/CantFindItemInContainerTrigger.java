package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CantFindItemInContainerTrigger implements EventTrigger {
    private static final Pattern PATTERN = PatternUtil.compile(".*Вы не видите '(.*)' в ([^\n]*)\\..*");
    private final EventDistributor _eventDistributor;

    public CantFindItemInContainerTrigger(EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public Event fireEvent(String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String item = matcher.group(1);
        final String container = matcher.group(2);
        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(AbstractTask task) {
                task.couldNotFindItemInContainer(item, container);
            }
        });

        return null;
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
