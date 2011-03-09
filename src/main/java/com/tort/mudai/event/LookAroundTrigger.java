package com.tort.mudai.event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LookAroundTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile("^(?:Вы поплелись на (?:север|юг|запад|восток)\\.\r\n)?\u001B\\[1\\;36m(.*)\u001B\\[0\\;37m$\\s\\s\\s.*");

    @Override
    public boolean matches(final String text) {
        final Matcher matcher = _pattern.matcher(text);

        return matcher.matches();
    }

    @Override
    public Event createEvent(final String text) {
        final Matcher matcher = _pattern.matcher(text);
        matcher.find();

        return new LookAroundEvent(matcher.group(1));
    }
}
