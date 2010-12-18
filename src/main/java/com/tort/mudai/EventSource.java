package com.tort.mudai;

/**
 * Created by IntelliJ IDEA.
 * User: olga
 * Date: 18.12.2010
 * Time: 19:57:39
 * To change this template use File | Settings | File Templates.
 */
public interface EventSource {
    void subscribe(AdapterEventListener listener);

    void unsubscribe(AdapterEventListener listener);
}
