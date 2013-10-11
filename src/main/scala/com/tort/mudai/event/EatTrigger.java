package com.tort.mudai.event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EatTrigger implements EventTrigger {
    private static final Pattern PATTERN = PatternUtil.compile(".*Вы съели ([^\n]*)\\..*");

    @Override
    public Event fireEvent(String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String food = matcher.group(1);

        return null;
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
