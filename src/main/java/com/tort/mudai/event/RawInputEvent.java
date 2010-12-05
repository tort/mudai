package com.tort.mudai.event;

import java.nio.CharBuffer;

public class RawInputEvent implements Event {
    private final CharBuffer _inCharBuffer;

    public RawInputEvent(final CharBuffer inCharBuffer) {
        _inCharBuffer = inCharBuffer;
    }

    public CharBuffer getInCharBuffer() {
        return _inCharBuffer;
    }
}
