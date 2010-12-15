package com.tort.mudai.task;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tort.mudai.Adapter;

public class Person {
    private Provider<SessionTask> _sessionProvider;
    private Adapter _adapter;

    @Inject
    public Person(final Provider<SessionTask> sessionProvider, final Adapter adapter) {
        _sessionProvider = sessionProvider;
        _adapter = adapter;

        _adapter.subscribe(_sessionProvider.get());
    }
}
