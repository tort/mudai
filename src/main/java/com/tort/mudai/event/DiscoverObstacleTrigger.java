package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscoverObstacleTrigger implements EventTrigger {
    private final EventDistributor _eventDistributor;
    private static final Pattern PATTERN = PatternUtil.compile("^(?:Закрыто\\..*)|(?:Закрыто \\(([^\n]*)\\)\\..*)");

    public DiscoverObstacleTrigger(EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public DiscoverObstacleEvent fireEvent(String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String obstacle = matcher.group(1);

        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(AbstractTask task) {
                task.discoverObstacle(obstacle);
            }
        });

        return new DiscoverObstacleEvent(obstacle);
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
