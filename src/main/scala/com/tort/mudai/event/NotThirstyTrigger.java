package com.tort.mudai.event;

import java.util.regex.Pattern;

public class NotThirstyTrigger implements EventTrigger {
    private static final Pattern PATTERN = PatternUtil.compile(".*Вы не чувствуете жажды\\..*");

    @Override
    public Event fireEvent(final String text) {
        return null;
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN.matcher(text).matches();
    }
}
