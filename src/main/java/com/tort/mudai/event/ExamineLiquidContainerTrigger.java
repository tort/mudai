package com.tort.mudai.event;

import com.tort.mudai.task.EventDistributor;
import sun.misc.Regexp;

import java.util.regex.Pattern;

public class ExamineLiquidContainerTrigger implements EventTrigger {
    private final Pattern _pattern = PatternUtil.compile("^");
    private final EventDistributor _eventDistributor;

    public ExamineLiquidContainerTrigger(final EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public void fireEvent(final String text) {

    }

    @Override
    public boolean matches(final String text) {
        return _pattern.matcher(text).matches();
    }
}
