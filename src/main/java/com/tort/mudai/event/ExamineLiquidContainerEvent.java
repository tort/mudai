package com.tort.mudai.event;

public class ExamineLiquidContainerEvent implements Event {
    private LiquidContainer.State _state;

    public ExamineLiquidContainerEvent(final LiquidContainer.State state) {
        _state = state;
    }

    public LiquidContainer.State getState() {
        return _state;
    }
}
