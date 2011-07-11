package com.tort.mudai.event;

import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
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
    public ExamineLiquidContainerEvent fireEvent(final String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String stateGroup = matcher.group(1);
        if(stateGroup.equals("Пусто"))
            return new ExamineLiquidContainerEvent(LiquidContainer.State.EMPTY);

        return null;
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN.matcher(text).matches();
    }
}
