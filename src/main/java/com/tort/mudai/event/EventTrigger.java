package com.tort.mudai.event;

public interface EventTrigger extends Trigger {
    Event createEvent(final String text);
}
