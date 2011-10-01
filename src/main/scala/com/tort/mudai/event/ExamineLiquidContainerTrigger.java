package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExamineLiquidContainerTrigger implements EventTrigger {
    public final static Pattern PATTERN = PatternUtil.compile(".*\r?\nСoстояние\\: (?:идеально|хорошо|средне|плохо|ужасно)\\.\r?\n" +
            "(.*)\\.\r?\n\r?\n[^\r?\n]*");
    private static final Pattern STATE_GROUP_PATTERN =
            PatternUtil.compile("Наполнена ((?:(?:меньше|больше), чем |примерно )(?:наполовину|на четверть) |почти полностью )?[^\\s]* жидкостью");

    private final EventDistributor _eventDistributor;

    public ExamineLiquidContainerTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public ExamineLiquidContainerEvent fireEvent(final String text) {
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();

        final String stateGroup = matcher.group(1);
        ExamineLiquidContainerEvent event;
        if (stateGroup.equals("Пусто")) {
            event = new ExamineLiquidContainerEvent(LiquidContainer.State.EMPTY);
        } else {
            if (stateGroup.startsWith("Наполнена")) {
                final Matcher stateMatcher = STATE_GROUP_PATTERN.matcher(stateGroup);
                stateMatcher.find();
                final String state = stateMatcher.group(1);

                if (state == null) {
                    event = new ExamineLiquidContainerEvent(LiquidContainer.State.FULL);
                } else if (state.equals("меньше, чем наполовину ")) {
                    event = new ExamineLiquidContainerEvent(LiquidContainer.State.LESS_THAN_HALF);
                } else if (state.equals("больше, чем наполовину ")) {
                    event = new ExamineLiquidContainerEvent(LiquidContainer.State.MORE_THAN_HALF);
                } else if (state.equals("больше, чем на четверть ")) {
                    event = new ExamineLiquidContainerEvent(LiquidContainer.State.MORE_THAN_QUARTER);
                } else if (state.equals("меньше, чем на четверть ")) {
                    event = new ExamineLiquidContainerEvent(LiquidContainer.State.LESS_THAN_QUARTER);
                } else if (state.equals("примерно наполовину ")) {
                    event = new ExamineLiquidContainerEvent(LiquidContainer.State.HALF);
                } else if (state.equals("почти полностью ")) {
                    event = new ExamineLiquidContainerEvent(LiquidContainer.State.ALMOST_FULL);
                } else {
                    throw new IllegalStateException("unknown case");
                }
            } else {
                throw new IllegalStateException("unknown case");
            }
        }

        final ExamineLiquidContainerEvent finalEvent = event;
        _eventDistributor.invoke(new Handler() {
            @Override
            public void handle(final AbstractTask task) {
                task.examineWaterContainer(finalEvent.getState());
            }
        });

        return event;
    }

    @Override
    public boolean matches(final String text) {
        return PATTERN.matcher(text).matches();
    }
}
