package com.tort.mudai.event;

import java.nio.CharBuffer;

public class RawReadEvent implements Event {
    private final String _inCharBuffer;

    public RawReadEvent(final String inCharBuffer) {
        _inCharBuffer = inCharBuffer;
    }

    public String getInCharBuffer() {
        return _inCharBuffer;
    }
}
