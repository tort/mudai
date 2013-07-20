package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Pattern;

public class LoginPromptTrigger implements EventTrigger<LoginPromptEvent> {
    private final Pattern _pattern = PatternUtil.compile(".*^Введите имя персонажа.*");

    @Override
    public boolean matches(String text) {
        return text != null && _pattern.matcher(text).matches();

    }

    @Override
    public LoginPromptEvent fireEvent(final String text) {
        return new LoginPromptEvent();
    }
}
