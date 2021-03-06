package com.tort.mudai.event;

public class AdapterExceptionEvent implements Event {
    private final Exception _exception;

    public AdapterExceptionEvent(final Exception e) {
        _exception = e;
    }

    public Exception getException() {
        return _exception;
    }
}
