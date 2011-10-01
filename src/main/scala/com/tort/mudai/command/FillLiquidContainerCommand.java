package com.tort.mudai.command;

public class FillLiquidContainerCommand implements RenderableCommand {
    private String liquidSource;
    private String containerShortName;

    public FillLiquidContainerCommand(String liquidSource, String containerShortName) {
        this.liquidSource = liquidSource;
        this.containerShortName = containerShortName;
    }

    @Override
    public String render() {
        return "напол " + containerShortName + " " + liquidSource;
    }
}
