package com.tort.mudai;

import com.tort.mudai.command.Command;

public interface CommandExecutor {
    void submit(Command command);
}
