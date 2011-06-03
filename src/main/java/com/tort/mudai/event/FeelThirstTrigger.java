package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Pattern;

public class FeelThirstTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile(".*^Вас мучает жажда\\.$.*");
    private final EventDistributor _eventDistributor;

    public FeelThirstTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public void fireEvent(final String text) {
        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(final AbstractTask task) {
                task.feelThirst();
            }
        });
    }

    @Override
    public boolean matches(final String text) {
        return _pattern.matcher(text).matches();
    }
}
