package com.tort.mudai.task;

import com.tort.mudai.command.BuyCommand;
import com.tort.mudai.command.Command;
import com.tort.mudai.mapper.Location;
import com.tort.mudai.mapper.Mapper;
import com.tort.mudai.mapper.MapperException;
import org.testng.annotations.Test;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
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
    public void pulse() throws MapperException {
        final BuyLiquidContainerTask task = createBuyLiquidContainerTask();

        final Command command = task.pulse();

        assertNull(command);
    }

    /**
     * if travel task is not terminated, forward pulses
     */
    public void pulseWhenTravelling() throws MapperException {
        final BuyLiquidContainerTask task = createBuyLiquidContainerTask();

        final Command command = task.pulse();

        verify(_travelTask).pulse();
    }

    public void init() throws MapperException {
        createBuyLiquidContainerTask();

        verifyTaskSubscribed(TravelTask.class);
    }

    public void afterTravel() throws MapperException {
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

    private BuyLiquidContainerTask createBuyLiquidContainerTask() throws MapperException {
        _eventDistributor = mock(EventDistributor.class);
        _mapper = mock(Mapper.class);
        when(_mapper.nearestWaterSource()).thenReturn(new Location());

        mockTravelTask();

        return new BuyLiquidContainerTask(_eventDistributor, _travelTaskFactory, _mapper);
    }

    private void mockTravelTask() {
        _travelTask = mock(TravelTask.class);
        _travelTaskFactory = mock(TravelTaskFactory.class);
        when(_travelTaskFactory.create(isA(Location.class))).thenReturn(_travelTask);
    }
}
