package com.tort.mudai.event;

import java.util.regex.Pattern;

public class FeelThirstTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile(".*^Вас мучает жажда\\.$.*");

    @SuppressWarnings({"UnusedDeclaration"})
    @Override
    public Event fireEvent(final String text) {
        return null;
    }

    @Override
    public boolean matches(final String text) {
        return _pattern.matcher(text).matches();
    }
}
