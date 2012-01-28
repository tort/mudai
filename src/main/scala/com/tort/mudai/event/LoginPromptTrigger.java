package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Pattern;

public class LoginPromptTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile(".*^Введите имя персонажа.*");
    private final EventDistributor _eventDistributor;

    public LoginPromptTrigger(EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public boolean matches(String text) {
        return text != null && _pattern.matcher(text).matches();

    }

    @Override
    public Event fireEvent(final String text) {
        _eventDistributor.invoke(new Handler() {
            @Override
            public void handle(AbstractTask task) {
                task.loginPrompt();
            }
        });

        return null;
    }
}
