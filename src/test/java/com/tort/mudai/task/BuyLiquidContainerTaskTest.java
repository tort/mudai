package com.tort.mudai.task;

import com.tort.mudai.command.BuyCommand;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Mapper;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test
public class BuyLiquidContainerTaskTest {
    private EventDistributor _eventDistributor;
    private TravelTaskFactory _travelTaskFactory;
    private Mapper _mapper;
    private TravelTask _travelTask;

    /**
     * default behaviour for pulse
     */
    public void pulse() {
        final BuyLiquidContainerTask task = createBuyLiquidContainerTask();

        final Command command = task.pulse();

        assertNull(command);
    }

    /**
     * if travel task is not terminated, forward pulses
     */
    public void pulseWhenTravelling(){
        final BuyLiquidContainerTask task = createBuyLiquidContainerTask();

        final Command command = task.pulse();

        verify(_travelTask).pulse();
    }

    public void init() {
        createBuyLiquidContainerTask();

        verifyTaskSubscribed(TravelTask.class);
    }

    public void afterTravel() {
        final BuyLiquidContainerTask containerTask = createBuyLiquidContainerTask();
        when(_travelTask.isTerminated()).thenReturn(true);

        Command command = containerTask.pulse();

        assertTrue(command instanceof BuyCommand);

        command = containerTask.pulse();

        assertNull(command);
    }

    private void verifyTaskSubscribed(final Class<? extends AbstractTask> taskClass) {
        verify(_eventDistributor).subscribe(isA(taskClass));
    }

    private BuyLiquidContainerTask createBuyLiquidContainerTask() {
        _eventDistributor = mock(EventDistributor.class);
        _mapper = mock(Mapper.class);

        mockTravelTask();

        return new BuyLiquidContainerTask(_eventDistributor, _travelTaskFactory, _mapper);
    }

    private void mockTravelTask() {
        _travelTask = mock(TravelTask.class);
        _travelTaskFactory = mock(TravelTaskFactory.class);
        when(_travelTaskFactory.create(anyString())).thenReturn(_travelTask);
    }
}
