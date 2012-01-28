package com.tort.mudai.event;

import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;
import com.tort.mudai.Handler;

import java.util.regex.Pattern;

public class PasswordPromptTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile("^Персонаж с таким именем уже существует.*");
    private final EventDistributor _eventDistributor;

    public PasswordPromptTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public boolean matches(final String text) {
        return text != null && _pattern.matcher(text).matches();

    }

    @Override
    public Event fireEvent(final String text) {
        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(final AbstractTask task) {
                task.passwordPrompt();
            }
        });

        return null;
    }
}
