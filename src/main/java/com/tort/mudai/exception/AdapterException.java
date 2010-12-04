package com.tort.mudai.exception;

import java.io.IOException;

public class AdapterException extends Exception {
    public AdapterException(final IOException e) {
        super(e);
    }
}
