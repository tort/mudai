package com.tort.mudai.command;

public class BuyCommand implements Command {
    private final String _item;
    private final Integer _number;

    public BuyCommand(final String item) {
        _item = item;
        _number = null;
    }

    public BuyCommand(String item, Integer number) {
        _item = item;
        _number = number;
    }

    @Override
    public String render() {
        String result = "купить ";
        if(_number != null){
            result += _number.toString() + " ";
        }
        return result + _item;
    }
}
