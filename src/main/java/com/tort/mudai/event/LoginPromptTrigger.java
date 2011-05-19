package com.tort.mudai.event;

import com.tort.mudai.task.EventDistributor;
import com.tort.mudai.task.Task;

import java.util.List;
import java.util.regex.Pattern;

public class LoginPromptTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile(".*^Введите имя персонажа.*");
    private final EventDistributor _eventDistributor;

    public LoginPromptTrigger(EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public boolean matches(String text) {
        if(text == null)
            return false;

        return _pattern.matcher(text).matches();
    }

    @Override
    public Event createEvent(final String text) {
        List<Task> targets = _eventDistributor.getTargets();
        for (Task target : targets) {
            target.loginPrompt();
        }
    }
}
