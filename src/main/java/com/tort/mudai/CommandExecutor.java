package com.tort.mudai;

import com.tort.mudai.command.Command;

/**
 * Created by IntelliJ IDEA.
 * User: olga
 * Date: 18.12.2010
 * Time: 19:58:11
 * To change this template use File | Settings | File Templates.
 */
public interface CommandExecutor {
    void submit(Command command);
}
