package com.tort.mudai;

import com.tort.mudai.command.Command;

/**
 * Created by IntelliJ IDEA.
 * User: olga
 * Date: 06.12.2010
 * Time: 23:58:09
 * To change this template use File | Settings | File Templates.
 */
public interface Adapter {
    int OUT_BUF_SIZE = 128;

    void subscribe(AdapterEventListener listener);

    void unsubscribe(AdapterEventListener listener);

    void start();

    void send(Command command);
}
