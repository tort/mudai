package com.tort.mudai.event;

import org.testng.annotations.Test;

import java.util.regex.Matcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class InventoryTriggerTest {
    private final String _text = "Вы несете:\r\n" +
            "ломоть хлеба  \u001B[1;37m<великолепно>\u001B[0;37m\r\n" +
            "\r\n" +
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m87M\u001B[0;37m 1499о Зауч:0 Вых:С^>";

    private final String _emptyInv = "Вы несете:\r\n" +
            " Вы ничего не несете.\r\n" +
            "\r\n" +
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m87M\u001B[0;37m 1499о Зауч:0 Вых:С^>";

    public void match() {
        assertTrue(new InventoryTrigger(null).matches(_text));
    }

    public void matchEmptyInv() {
        assertTrue(new InventoryTrigger(null).matches(_emptyInv));
    }

    public void noItems(){
        Matcher matcher = InventoryTrigger.PATTERN.matcher(_emptyInv);
        matcher.find();

        final String itemsGroup = matcher.group(1);
        assertEquals(itemsGroup, " Вы ничего не несете.");
    }

    public void items(){
        Matcher matcher = InventoryTrigger.PATTERN.matcher(_emptyInv);
        matcher.find();

        final String[] itemsGroup = matcher.group(1).split(InventoryTrigger.DELIMETER);
        assertEquals(itemsGroup.length, 1);
    }
}
