package com.tort.mudai;

import com.tort.mudai.command.RenderableCommand;

public interface CommandExecutor {
    void submit(RenderableCommand command);
}
