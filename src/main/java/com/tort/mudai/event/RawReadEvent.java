package com.tort.mudai.event;

import java.nio.CharBuffer;

public class RawReadEvent implements Event {
    private final CharBuffer _inCharBuffer;

    public RawReadEvent(final CharBuffer inCharBuffer) {
        _inCharBuffer = inCharBuffer;
    }

    public CharBuffer getInCharBuffer() {
        return _inCharBuffer;
    }
}
