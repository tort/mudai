package com.tort.mudai.event;

import java.util.regex.Pattern;

public class EmptyLiquidContainerTrigger implements EventTrigger {
    private static final Pattern PATTERN = PatternUtil.compile("^Пусто.\r?\n" +
            "\r?\n" +
            "[^\n]*$");

    @Override
    public Event fireEvent(final String text) {
        return null;
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN.matcher(text).matches();
    }
}
