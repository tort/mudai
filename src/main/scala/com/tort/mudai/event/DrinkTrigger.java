package com.tort.mudai.event;

import java.util.regex.Pattern;

public class DrinkTrigger implements EventTrigger {
    private static final Pattern PATTERN = PatternUtil.compile(".*Вы выпили (?:[^\n]*) из (?:[^\n]*)\\..*");

    @Override
    public Event fireEvent(String text) {
        return null;
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
