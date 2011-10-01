package com.tort.mudai.task;

import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.mapper.Location;

public interface GoAndDoTaskFactory {
    GoAndDoTask create(Location to, RenderableCommand command, TaskTerminateCallback taskTerminateCallback);
}
