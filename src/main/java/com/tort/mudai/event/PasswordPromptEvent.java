package com.tort.mudai.event;

import java.util.regex.Pattern;

public class PasswordPromptEvent implements MatchingEvent {
    private final Pattern _pattern = PatternUtil.compile("^Персонаж с таким именем уже существует.*");

    @Override
    public boolean matches(final String text) {
        if(text == null){
            return false;
        }

        return _pattern.matcher(text).matches();
    }
}
