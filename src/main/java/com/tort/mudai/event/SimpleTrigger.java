package com.tort.mudai.event;

import java.util.regex.Pattern;

public class SimpleTrigger implements Trigger {
    private Pattern _pattern;
    private String _action;

    public SimpleTrigger(final String regex, final String action) {
        _pattern = PatternUtil.compile(regex);
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
