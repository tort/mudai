package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LookAroundTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile("^(?:Вы поплелись на (?:север|юг|запад|восток)\\.\r\n)?\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s.*");
    private final EventDistributor _eventDistributor;

    public LookAroundTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public boolean matches(final String text) {
        final Matcher matcher = _pattern.matcher(text);

        return matcher.matches();
    }

    @Override
    public void fireEvent(final String text) {
        final Matcher matcher = _pattern.matcher(text);
        matcher.find();

        final String locationTitle = matcher.group(1);

        _eventDistributor.invoke(new Handler<LookAroundEvent>(){
            @Override
            public void handle(final AbstractTask task) {
                task.lookAround(locationTitle);
            }
        });
    }
}
