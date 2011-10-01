package com.tort.mudai.telnet;

public class ParseResult {
    private final String _text;
    private final String _prompt;

    public ParseResult(final String text, final String prompt) {
        _text = text;
        _prompt = prompt;
    }

    public ParseResult(final String text) {
        this(text, null);
    }

    public String getPrompt() {
        return _prompt;
    }

    public String getText() {
        return _text;
    }
}
