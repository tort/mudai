package com.tort.mudai.event;

import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExamineLiquidContainerTrigger implements EventTrigger {
    public final static Pattern PATTERN = PatternUtil.compile(".*\nСoстояние\\: (?:идеально|хорошо|средне|плохо|ужасно)\\.\n" +
                                                              "(.*)\\.\n\n[^\n]*");
    private static final Pattern STATE_GROUP_PATTERN =
            PatternUtil.compile("(?:Наполнена ((?:меньше|больше), чем наполовину )?)[^\\s]* жидкостью");
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

        if(stateGroup.startsWith("Наполнена")){
            final Matcher stateMatcher = STATE_GROUP_PATTERN.matcher(stateGroup);
            stateMatcher.find();
            final String state = stateMatcher.group(1);

            if(state.isEmpty())
                return new ExamineLiquidContainerEvent(LiquidContainer.State.FULL);

            if(state.equals("меньше, чем наполовину "))
                return new ExamineLiquidContainerEvent(LiquidContainer.State.LESS_THAN_HALF);

            if(state.equals("больше, чем наполовину "))
                return new ExamineLiquidContainerEvent(LiquidContainer.State.MORE_THAN_HALF);

            if(state.equals("примерно наполовину "))
                return new ExamineLiquidContainerEvent(LiquidContainer.State.HALF);
        }

        throw new IllegalStateException("unknown case");
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN.matcher(text).matches();
    }
}
