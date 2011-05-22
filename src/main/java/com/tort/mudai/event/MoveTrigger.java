package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile("^Вы поплелись на (север|юг|запад|восток)\\.$.*");
    private final EventDistributor _eventDistributor;

    public MoveTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public void fireEvent(final String text) {
        final Matcher matcher = _pattern.matcher(text);
        matcher.find();
        final String direction = matcher.group(1);

        _eventDistributor.invoke(new Handler<MoveEvent>(){
            @Override
            public void handle(final AbstractTask task) {
                task.move(direction);
            }
        });
    }

    @Override
    public boolean matches(final String text) {
        return _pattern.matcher(text).matches();
    }
}
