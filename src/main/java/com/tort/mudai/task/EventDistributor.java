package com.tort.mudai.task;

import com.tort.mudai.Handler;
import com.tort.mudai.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDistributor {
    private Map<Class, Handler> _events = new HashMap<Class, Handler>();
    private List<AbstractTask> _tasks = new ArrayList<AbstractTask>();

    public void invoke(Handler handler) {
        for (AbstractTask task : _tasks) {
            handler.handle(task);
        }
    }

    public void subscribe(AbstractTask task) {
        _tasks.add(task);
    }

    public void programmerError(final Throwable e){
        invoke(new Handler<ProgrammerErrorEvent>() {
            @Override
            public void handle(AbstractTask task) {
                task.programmerError(e);
            }
        });
    }

    public void adapterException(final Exception e){
        invoke(new Handler<AdapterExceptionEvent>() {
            public void handle(AbstractTask task) {
                task.adapterException(e);
            }
        });
    }

    public void connectionClose(){
        invoke(new Handler() {
            @Override
            public void handle(AbstractTask task) {
                task.connectionClosed();
            }
        });
    }

    public List<AbstractTask> getTargets() {
        return _tasks;
    }

    public void rawReadEvent(final String ga_block) {
        invoke(new Handler<RawReadEvent>(){
            @Override
            public void handle(final AbstractTask task) {
                task.rawRead(ga_block);
            }
        });
    }
}
