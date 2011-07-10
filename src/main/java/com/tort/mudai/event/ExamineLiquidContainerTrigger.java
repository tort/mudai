package com.tort.mudai.event;

import com.tort.mudai.task.EventDistributor;

import java.util.regex.Pattern;

public class ExamineLiquidContainerTrigger implements EventTrigger {
    public final static Pattern PATTERN = PatternUtil.compile(".*\nСoстояние\\: (?:идеально|хорошо|средне|плохо|ужасно)\\.\n" +
            "(.*)\\.\n\n[^\n]*");
//            "(Наполнена (?:(?:меньше|больше) чем наполовину )?)\\w+ жидкостью\\.\n");
    private final EventDistributor _eventDistributor;

    public ExamineLiquidContainerTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public void fireEvent(final String text) {

    }

    @Override
    public boolean matches(final String text) {
        return PATTERN.matcher(text).matches();
    }
}
