package com.tort.mudai.event;

public interface EventTrigger extends Trigger {
    void fireEvent(final String text);
}
