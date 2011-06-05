package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.AbstractTask;
import com.tort.mudai.task.EventDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryTrigger implements EventTrigger {
    public final static Pattern PATTERN = PatternUtil.compile("^Вы несете:\r\n(.*)\r\n\r\n[^\n\r]*$");
    public static final String DELIMETER = "\n";

    private final EventDistributor _eventDistributor;

    public InventoryTrigger(EventDistributor eventDistributor) {
        _eventDistributor = eventDistributor;
    }

    @Override
    public void fireEvent(String text) {
        String[] inventory = {};
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();
        final String invGroup = matcher.group(1);
        if(!invGroup.equals(" Вы ничего не несете\\.")){
            inventory = invGroup.split(DELIMETER);
        }

        final String[] finalInventory = inventory;
        _eventDistributor.invoke(new Handler(){
            @Override
            public void handle(AbstractTask task) {
                task.inventory(finalInventory);
            }
        });
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
