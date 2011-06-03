package com.tort.mudai.task;

import com.tort.mudai.RoomSnapshot;

public abstract class AbstractTask implements Task {
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
}
