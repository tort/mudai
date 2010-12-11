package com.tort.mudai.event;

import java.util.regex.Pattern;

public class LoginPromptEvent implements MatchingEvent {
    private final Pattern _pattern = Pattern.compile(".*^Введите имя персонажа.*", Pattern.MULTILINE | Pattern.DOTALL);

    @Override
    public boolean matches(String text) {
        if(text == null)
            return false;

        return _pattern.matcher(text).matches();
    }
}
