package com.tort.mudai.task;

import com.tort.mudai.PersonProperties;
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

@SuppressWarnings({"JavaDoc", "FieldCanBeLocal"})
@Test
public class BuyLiquidContainerTaskTest {
    private EventDistributor _eventDistributor;
    private TravelTaskFactory _travelTaskFactory;
    private Mapper _mapper;
    private TravelTask _travelTask;
    private PersonProperties _personProperties;
    private static final String WATER_CONTAINER = "фляга";

    /**
     * default behaviour for pulse
     */
    public void pulse() throws MapperException {
        final FillLiquidContainerTask task = createBuyLiquidContainerTask();

        final Command command = task.pulse();

        assertNull(command);
    }

    /**
     * if travel task is not terminated, forward pulses
     */
    public void pulseWhenTravelling() throws MapperException {
        final FillLiquidContainerTask task = createBuyLiquidContainerTask();

        task.pulse();

        verify(_travelTask).pulse();
    }

    public void init() throws MapperException {
        createBuyLiquidContainerTask();

        verifyTaskSubscribed(TravelTask.class);
    }

    public void afterTravel() throws MapperException {
        final FillLiquidContainerTask containerTask = createBuyLiquidContainerTask();
        when(_travelTask.isTerminated()).thenReturn(true);

        Command command = containerTask.pulse();

        assertTrue(command instanceof BuyCommand);

        command = containerTask.pulse();

        assertNull(command);
    }

    private void verifyTaskSubscribed(final Class<? extends AbstractTask> taskClass) {
        verify(_eventDistributor).subscribe(isA(taskClass));
    }

    private FillLiquidContainerTask createBuyLiquidContainerTask() throws MapperException {
        _eventDistributor = mock(EventDistributor.class);
        _mapper = mock(Mapper.class);
        when(_mapper.nearestWaterSource()).thenReturn(new Location());

        _personProperties = mock(PersonProperties.class);
        when(_personProperties.getLiquidContainer()).thenReturn(WATER_CONTAINER);

        mockTravelTask();

        return new FillLiquidContainerTask(_eventDistributor, _travelTaskFactory, _mapper, _personProperties, null);
    }

    private void mockTravelTask() {
        _travelTask = mock(TravelTask.class);
        _travelTaskFactory = mock(TravelTaskFactory.class);
        when(_travelTaskFactory.create(isA(Location.class), null)).thenReturn(_travelTask);
    }
}
