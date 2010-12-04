package com.tort.mudai;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Adapter {
    private AdapterEventListener _adapterEventListener = new EmptyEventListener();

    private void setEventListener(final AdapterEventListener adapterEventListener) {
        _adapterEventListener = adapterEventListener;
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MudaiModule());
        Adapter adapter = injector.getInstance(Adapter.class);

        adapter.setEventListener(new AdapterEventListener(){});
        adapter.start();
    }

    private void start() {
        
    }
}
