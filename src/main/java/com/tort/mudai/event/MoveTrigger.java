package com.tort.mudai.event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile("^Вы поплелись на (север|юг|запад|восток)\\.$.*");

    @Override
    public Event createEvent(final String text) {
        final Matcher matcher = _pattern.matcher(text);
        matcher.find();
        final String direction = matcher.group(1);

        return new MoveEvent(direction);
    }

    @Override
    public boolean matches(final String text) {
        return _pattern.matcher(text).matches();
    }
}
