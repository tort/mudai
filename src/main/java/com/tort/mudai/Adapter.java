package com.tort.mudai;

import com.tort.mudai.command.Command;

public interface Adapter {
    int OUT_BUF_SIZE = 128;

    void subscribe(AdapterEventListener listener);

    void unsubscribe(AdapterEventListener listener);

    void start();

    void send(Command command);
}
