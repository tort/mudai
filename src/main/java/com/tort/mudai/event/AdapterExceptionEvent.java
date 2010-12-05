package com.tort.mudai.event;

import java.io.IOException;

public class AdapterExceptionEvent implements Event {
    private final IOException _exception;

    public AdapterExceptionEvent(final IOException e) {
        _exception = e;
    }

    public IOException getException() {
        return _exception;
    }
}
