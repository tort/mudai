package com.tort.mudai;

import com.tort.mudai.exception.AdapterException;

import java.nio.CharBuffer;

public class EmptyEventListener implements AdapterEventListener {
    public void networkException(final AdapterException e) {
    }

    public void connectionClosed() {
    }

    public void rawInput(final CharBuffer charBuffer) {
    }
}
