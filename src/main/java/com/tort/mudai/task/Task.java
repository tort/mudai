package com.tort.mudai.task;

import com.tort.mudai.AdapterEventListener;
import com.tort.mudai.command.Command;

public interface Task extends AdapterEventListener {
    Command pulse();
}
