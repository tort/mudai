package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CantFindItemTrigger implements EventTrigger {
    private final static Pattern PATTERN = PatternUtil.compile(".*Вы не видите '([^\\s].*)'\\..*");
    private final EventDistributor _eventDistributor;

    public CantFindItemTrigger(EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public Event fireEvent(String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String item = matcher.group(1);

        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(AbstractTask task) {
                task.couldNotFindItem(item);
            }
        });

        return null;
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
