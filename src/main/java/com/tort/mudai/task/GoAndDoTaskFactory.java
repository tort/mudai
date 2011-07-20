package com.tort.mudai.task;

import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Location;

public interface GoAndDoTaskFactory {
    GoAndDoTask create(Location to, Command command, TaskTerminateCallback taskTerminateCallback);
}
