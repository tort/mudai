package com.tort.mudai;

public interface EventSource {
    void subscribe(AdapterEventListener listener);

    void unsubscribe(AdapterEventListener listener);
}
