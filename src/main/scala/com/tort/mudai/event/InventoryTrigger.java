package com.tort.mudai.event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryTrigger implements EventTrigger {
    public final static Pattern PATTERN = PatternUtil.compile("^Вы несете:\r\n(.*)\r\n\r\n[^\n\r]*$");
    public static final String DELIMETER = "\n";

    @Override
    public Event fireEvent(String text) {
        String[] inventory = {};
        final Matcher matcher = PATTERN.matcher(text);
        matcher.find();
        final String invGroup = matcher.group(1);
        if (!invGroup.equals(" Вы ничего не несете\\.")) {
            inventory = invGroup.split(DELIMETER);
        }
        for (int i = 0; i < inventory.length; i++) {
            String item = inventory[i];

            int stateIndex = item.indexOf(" c ");
            if (stateIndex > -1) {
                inventory[i] = item.substring(0, stateIndex);
            } else {
                stateIndex = item.indexOf("\u001B[1;37m");
                if (stateIndex > -1) {
                    inventory[i] = item.substring(0, stateIndex - 2);
                }
            }
        }

        final String[] finalInventory = inventory;

        return null;
    }

    @Override
    public boolean matches(String text) {
        return PATTERN.matcher(text).matches();
    }
}
