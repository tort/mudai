package com.tort.mudai.task;

import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.command.Command;
import com.tort.mudai.event.LiquidContainer;

public abstract class AbstractTask implements Task {
    public void move(String direction, String locationTitle) {
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

    @Override
    public Command pulse() {
        return null;
    }

    public void examineWaterContainer(final LiquidContainer.State state){

    }

    public void feelNotThirsty() {

    }
}
