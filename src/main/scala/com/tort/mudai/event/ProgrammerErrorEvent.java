package com.tort.mudai.event;

public class ProgrammerErrorEvent implements Event {
    private final Throwable _exception;

    public ProgrammerErrorEvent(final Throwable exception) {
        _exception = exception;
    }

    public Throwable getException() {
        return _exception;
    }
}
