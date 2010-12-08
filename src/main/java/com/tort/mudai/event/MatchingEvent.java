package com.tort.mudai.event;

public interface MatchingEvent extends Event {
    boolean matches(String text);
}
