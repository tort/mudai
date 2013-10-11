package com.tort.mudai.event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CantFindItemInContainerTrigger implements EventTrigger {
    private static final Pattern PATTERN = PatternUtil.compile(".*Вы не видите '(.*)' в ([^\n]*)\\..*");

    @Override
    public Event fireEvent(String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String item = matcher.group(1);
        final String container = matcher.group(2);

        return null;
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
