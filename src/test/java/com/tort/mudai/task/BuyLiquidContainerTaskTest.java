package com.tort.mudai.task;

import com.tort.mudai.command.Command;
import com.tort.mudai.command.FillLiquidContainerCommand;
import org.testng.annotations.Test;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertTrue;

@Test
public class BuyLiquidContainerTaskTest {
    private EventDistributor _eventDistributor;

    public void init() {
        createBuyLiquidContainerTask();

        new BuyLiquidContainerTask();

        verifyTaskSubscribed(TravelTask.class);
    }

    private void createBuyLiquidContainerTask() {
        _eventDistributor = mock(EventDistributor.class);
    }

    public void afterTravel() {
        final BuyLiquidContainerTask containerTask = new BuyLiquidContainerTask();

        final Command command = containerTask.pulse();

        assertTrue(command instanceof FillLiquidContainerCommand);
    }

    private void verifyTaskSubscribed(final Class<? extends AbstractTask> taskClass) {
        verify(_eventDistributor).subscribe(isA(taskClass));
    }
}
