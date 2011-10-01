package com.tort.mudai.command;

public class RawWriteCommand implements RenderableCommand {
    private final String _charBuffer;

    public RawWriteCommand(final String charBuffer) {
        _charBuffer = charBuffer;
    }

    @Override
    public String render() {
        return _charBuffer;
    }
}
