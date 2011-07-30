package com.tort.mudai;

import com.tort.mudai.command.Command;
import com.tort.mudai.task.EatTask;
import com.tort.mudai.task.ProvisionTask;
import com.tort.mudai.task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PulseDistributor {
    private static final Command EMPTY_COMMAND = null;

    private final List<Task> _subtasks = new CopyOnWriteArrayList<Task>();

    public Command invoke() {
        Command command = null;
        for (Task task : _subtasks) {
            command = task.pulse();
            if (command != EMPTY_COMMAND) {
                return command;
            }

            if (task.isInitializing())
                break;
        }

        List<Task> toDelete = new ArrayList();
        for (Task subtask : _subtasks) {
            if (subtask.isTerminated())
                toDelete.add(subtask);
        }

        for (Task task : toDelete) {
            _subtasks.remove(task);
        }
        return command;
    }

    public void subscribe(Task eatTask) {
        _subtasks.add(eatTask);
    }
}
