package com.tort.mudai.mapper;

public class MockPersister implements Persister {
    @Override
    public void persist(final Location location) {
        System.out.println("PERSISTING " + location.getTitle());
    }
}
