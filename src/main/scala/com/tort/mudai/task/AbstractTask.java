package com.tort.mudai.task;

import com.tort.mudai.RoomSnapshot;
import com.tort.mudai.command.RenderableCommand;
import com.tort.mudai.event.LiquidContainer;
import com.tort.mudai.mapper.Direction;
import com.tort.mudai.persistance.Stat;

public abstract class AbstractTask implements Task {
    public void adapterException(Exception e){
//        throw new NotImplementedException();
    }

    public void viewStat(Stat stat) {

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

    public void glance(Direction direction, RoomSnapshot roomSnapshot){
//        throw new NotImplementedException();
    }

    public void glance(RoomSnapshot roomSnapshot){
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
    public RenderableCommand pulse() {
        return null;
    }

    public void examineWaterContainer(final LiquidContainer.State state){

    }

    public void feelNotThirsty() {

    }

    public void couldNotFindItem(String item) {

    }

    public void couldNotFindItemInContainer(String item, String container) {

    }

    public void feelNotHungry() {

    }

    public void eat(String food) {

    }

    public void drink() {

    }

    public void discoverObstacle(String obstacle) {

    }

    public void kill(String target) {

    }
    
    public void takeItem(String item) {

    }
}
