package com.tort.mudai.telnet;

import java.nio.CharBuffer;

public class TelnetParser {
    public String parse(final CharBuffer inCharBuffer) {
        final StringBuilder builder = new StringBuilder(inCharBuffer);

        return builder.toString();
    }
}
