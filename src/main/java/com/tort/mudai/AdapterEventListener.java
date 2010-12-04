package com.tort.mudai;

import com.tort.mudai.exception.AdapterException;

import java.nio.CharBuffer;

public interface AdapterEventListener {
    void networkException(AdapterException e);

    void connectionClosed();

    void rawInput(CharBuffer charBuffer);
}
