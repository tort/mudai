package com.tort.mudai.task;

import com.tort.mudai.command.Command;
import org.testng.annotations.Test;

@Test
public class GoAndBuyWaterContainerTaskTest {
    public void start(){
        final GoAndBuyWaterContainerTask task = new GoAndBuyWaterContainerTask();

        final Command command = task.pulse();

    }
}
