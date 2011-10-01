package com.tort.mudai.command;

public class ExamineItemCommand implements RenderableCommand {
    private String _item;

    public ExamineItemCommand(final String item) {
        _item = item;
    }

    @Override
    public String render() {
        return "осм " + _item;
    }
}
