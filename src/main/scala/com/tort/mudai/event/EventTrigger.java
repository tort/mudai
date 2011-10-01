package com.tort.mudai.event;

public interface EventTrigger<T extends Event> extends Trigger {
    T fireEvent(final String text);
}
