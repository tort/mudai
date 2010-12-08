package com.tort.mudai.telnet;

import java.nio.CharBuffer;

public class TelnetParser {
    public ParseResult parse(final CharBuffer inCharBuffer) {
        final StringBuilder builder = new StringBuilder(inCharBuffer);
        final int lastTerminator = builder.lastIndexOf("\n");
        if(lastTerminator < 0){
            return new ParseResult(builder.toString());
        }
        
        String text = builder.substring(0, lastTerminator - 1);
        String prompt = builder.substring(lastTerminator + 1, builder.length() - 1);

        return new ParseResult(text, prompt);
    }
}
