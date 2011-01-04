package com.tort.mudai.event;

import java.util.regex.Pattern;

public class LoginPromptTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile(".*^Введите имя персонажа.*");

    @Override
    public boolean matches(String text) {
        if(text == null)
            return false;

        return _pattern.matcher(text).matches();
    }

    @Override
    public Event createEvent(final String text) {
        return new LoginPromptEvent();
    }
}
