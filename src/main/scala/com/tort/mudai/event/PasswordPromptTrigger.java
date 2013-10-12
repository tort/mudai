package com.tort.mudai.event;

import java.util.regex.Pattern;

public class PasswordPromptTrigger implements EventTrigger<PasswordPromptEvent> {
    private final Pattern _pattern = PatternUtil.compile("^Персонаж с таким именем уже существует.*");

    @Override
    public boolean matches(final String text) {
        return text != null && _pattern.matcher(text).matches();

    }

    @Override
    public PasswordPromptEvent fireEvent(final String text) {
        return new PasswordPromptEvent();
    }
}
