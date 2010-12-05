package com.tort.mudai.command;

import java.nio.CharBuffer;

public class RawWriteCommand implements Command {
    private final String _charBuffer;

    public RawWriteCommand(final String charBuffer) {
        _charBuffer = charBuffer;
    }

    public String getCharBuffer() {
        return _charBuffer;
    }
}
