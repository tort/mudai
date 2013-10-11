package com.tort.mudai.event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CantFindItemTrigger implements EventTrigger {
    private final static Pattern PATTERN = PatternUtil.compile(".*Вы не видите '([^\\s].*)'\\..*");

    @Override
    public Event fireEvent(String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String item = matcher.group(1);

        return null;
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
