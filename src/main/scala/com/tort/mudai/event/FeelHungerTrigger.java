package com.tort.mudai.event;

import java.util.regex.Pattern;

public class FeelHungerTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile(".*^Вы голодны\\.$.*");

    @Override
    public Event fireEvent(final String text) {
        return null;
    }

    @Override
    public boolean matches(final String text) {
        return _pattern.matcher(text).matches();
    }
}
