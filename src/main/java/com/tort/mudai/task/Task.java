package com.tort.mudai.task;

import com.tort.mudai.AdapterEventListener;

public interface Task extends AdapterEventListener {
    void move(String direction);
}
