package com.tort.mudai.event;

import java.util.regex.Pattern;

public class Trigger implements MatchingEvent {
    private Pattern _pattern;
    private String _action;

    public Trigger(final String regex, final String action) {
        _pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
        _action = action;
    }

    @Override
    public boolean matches(final String text) {
        if(text == null)
            return false;

        return _pattern.matcher(text).matches();
    }

    public String getAction() {
        return _action;
    }
}
