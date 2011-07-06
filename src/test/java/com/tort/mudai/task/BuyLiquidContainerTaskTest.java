package com.tort.mudai.task;

import com.tort.mudai.command.Command;
import org.testng.annotations.Test;

@Test
public class BuyLiquidContainerTaskTest {
    public void start(){
        final RetrieveLiquidContainerTask task = new RetrieveLiquidContainerTask();

        final Command command = task.pulse();

    }
}
