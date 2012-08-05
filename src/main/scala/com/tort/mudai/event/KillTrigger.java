package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillTrigger implements EventTrigger {
    private final EventDistributor _eventDistributor;
    private static final Pattern PATTERN = PatternUtil.compile(".*\u001B\\[0\\;37m([^\n]*) (?:мертв|мертва|мертвы), (?:его|ее) душа медленно подымается в небеса.\r?\n" +
            "Ваш опыт повысился на ([1234567890]*) (?:очка|очков?)\\.\r?\n.*");

    public KillTrigger(EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public Event fireEvent(String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();
        final String target = matcher.group(1);

        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(AbstractTask task) {
                task.kill(target);
            }
        });

        return null;
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
