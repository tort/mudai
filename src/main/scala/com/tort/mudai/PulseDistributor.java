package com.tort.mudai;

import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PulseDistributor {
    private static final RenderableCommand EMPTY_COMMAND = null;

    private final List<Task> _subtasks = new CopyOnWriteArrayList<Task>();

    public RenderableCommand pulse() {
        RenderableCommand command = null;
        for (Task task : _subtasks) {
            command = task.pulse();
            if (command != EMPTY_COMMAND) {
                return command;
            }

            if (task.isInitializing())
                break;
        }

        List<Task> terminatedTasks = new ArrayList();
        for (Task subtask : _subtasks) {
            if (subtask.isTerminated())
                terminatedTasks.add(subtask);
        }

        for (Task terminatedTask : terminatedTasks) {
            _subtasks.remove(terminatedTask);
        }

        return command;
    }

    public void subscribe(Task task) {
        _subtasks.add(task);
    }
}
