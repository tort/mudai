package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveTrigger implements EventTrigger {
    public static final DirectionLister lister = new DirectionLister();
    public static final Pattern PATTERN = PatternUtil.compile("^Вы поплелись на (" + lister.listDirections() + ")\\.\r?\n" +
            "\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s.*\r?\n\r?\n.*");
    private final EventDistributor _eventDistributor;

    public MoveTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public Event fireEvent(final String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();
        final String direction = matcher.group(1);
        final String locationTitle = matcher.group(2);


        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(final AbstractTask task) {
                task.move(direction, locationTitle);
            }
        });

        return null;
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN.matcher(text).matches();
    }
}
