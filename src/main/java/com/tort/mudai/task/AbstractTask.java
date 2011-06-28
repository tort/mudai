package com.tort.mudai.task;

import com.tort.mudai.RoomSnapshot;

public abstract class AbstractTask implements Task {
    protected Status _status = Status.INIT;

    public void move(String direction) {
//        throw new NotImplementedException();
    }

    public void adapterException(Exception e){
//        throw new NotImplementedException();
    }

    public void connectionClosed(){
//        throw new NotImplementedException();
    }

    public void rawRead(String buffer){
//        throw new NotImplementedException();
    }

    public void programmerError(Throwable exception) {
//        throw new NotImplementedException();
    }

    public void lookAround(RoomSnapshot locationTitle){
//        throw new NotImplementedException();
    }

    public void loginPrompt(){
//        throw new NotImplementedException();
    }

    public void passwordPrompt(){
//        throw new NotImplementedException();
    }

    public void feelThirst() {
    }

    public void feelHunger() {
    }

    public void inventory(final String[] items) {
    }

    public void waterContainerAlmostEmpty() {
    }

    public void waterContainerFull() {
    }

    @Override
    public boolean isInit() {
        return _status == Status.INIT;
    }

    @Override
    public boolean isTerminated() {
        return _status == Status.TERMINATED;
    }

    @Override
    public Status status() {
        return _status;
    }
}
