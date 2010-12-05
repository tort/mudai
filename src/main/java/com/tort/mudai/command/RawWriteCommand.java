package com.tort.mudai.command;

import java.nio.CharBuffer;

public class RawWriteCommand implements Command {
    private final CharBuffer _charBuffer;

    public RawWriteCommand(final CharBuffer charBuffer) {
        _charBuffer = charBuffer;
    }

    public CharBuffer getCharBuffer() {
        return _charBuffer;
    }
}
