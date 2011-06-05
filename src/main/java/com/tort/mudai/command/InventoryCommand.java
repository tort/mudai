package com.tort.mudai.command;

public class InventoryCommand implements Command{
    private static final String INVENTORY_COMMAND_TEXT = "инв";

    @Override
    public String render() {
        return INVENTORY_COMMAND_TEXT;
    }
}
