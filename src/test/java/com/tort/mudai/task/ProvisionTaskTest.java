package com.tort.mudai.task;

import com.google.inject.Provider;
import com.tort.mudai.PersonProperties;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.ExamineItemCommand;
import com.tort.mudai.command.InventoryCommand;
import com.tort.mudai.event.LiquidContainer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test
public class ProvisionTaskTest {
    private static final String WATER_CONTAINER = "мех";
    private EventDistributor _eventDistributor;
    private StatedTask _provisionTask;
    private Provider<BuyLiquidContainerTask> _retrieveLiquidContainerTaskProvider;
    private Provider<FillLiquidContainerTask> _fillLiquidContainerTaskProvider;
    private Provider<DrinkTask> _drinkTaskProvider;
    private PersonProperties _personProperties;

    public void enterWorld() throws Exception {
        createProvisionTask();
        Command command = _provisionTask.pulse();

        assertTrue(command instanceof InventoryCommand);

        command = _provisionTask.pulse();

        assertNull(command);
    }

    public void waterContainerAbsent() {
        createProvisionTask();
        _provisionTask.inventory(new String[]{});

        _provisionTask.pulse();

        verify(_eventDistributor).subscribe(isA(BuyLiquidContainerTask.class));
    }

    public void waterContainerPresent() {
        _provisionTask.inventory(new String[]{WATER_CONTAINER});
        final Command command = _provisionTask.pulse();

        assertTrue(command instanceof ExamineItemCommand);
    }

    public void containerFull() {
        _provisionTask.examineWaterContainer(LiquidContainer.State.FULL);

        verifyTaskSubscribed(DrinkTask.class);
    }

    private void verifyTaskSubscribed(final Class<? extends AbstractTask> taskClass) {
        verify(_eventDistributor).subscribe(isA(taskClass));
    }

    public void containerAlmostEmpty() {
        _provisionTask.examineWaterContainer(LiquidContainer.State.LESS_THAN_HALF);

        verifyTaskSubscribed(FillLiquidContainerTask.class);
    }

    public void afterBuyWaterContainer() {
        createProvisionTask();
        _provisionTask.inventory(new String[]{WATER_CONTAINER});

        final Command command = _provisionTask.pulse();

        assertTrue(command instanceof ExamineItemCommand);
    }

    public void afterFillWaterContainer() {
        createProvisionTask();
        _provisionTask.examineWaterContainer(LiquidContainer.State.FULL);

        _provisionTask.pulse();

        verifyTaskSubscribed(DrinkTask.class);
    }

    private FillLiquidContainerTask createFillWaterContainerTaskJustTerminated() {
        //TODO generalize creating terminated task
        final FillLiquidContainerTask task = mock(FillLiquidContainerTask.class);
        when(task.isTerminated()).thenReturn(true);
        return task;
    }

    private BuyLiquidContainerTask createBuyWaterContainerTaskJustTerminated() {
        //TODO generalize creating terminated task
        final BuyLiquidContainerTask task = mock(BuyLiquidContainerTask.class);
        when(task.isTerminated()).thenReturn(true);
        return task;
    }

    private void createProvisionTask() {
        final BuyLiquidContainerTask buyLiquidTask = createBuyWaterContainerTaskJustTerminated();
        when(_retrieveLiquidContainerTaskProvider.get()).thenReturn(buyLiquidTask);
        final FillLiquidContainerTask fillLiquidTask = createFillWaterContainerTaskJustTerminated();
        when(_fillLiquidContainerTaskProvider.get()).thenReturn(fillLiquidTask);
        when(_drinkTaskProvider.get()).thenReturn(new DrinkTask());
        when(_personProperties.getLiquidContainer()).thenReturn(WATER_CONTAINER);

        _eventDistributor = mock(EventDistributor.class);
        _provisionTask = new ProvisionTask(_eventDistributor,
                _retrieveLiquidContainerTaskProvider,
                _fillLiquidContainerTaskProvider,
                _drinkTaskProvider,
                _personProperties);
    }

    @SuppressWarnings({"unchecked"})
    @BeforeMethod
    protected void setUp() throws Exception {
        _retrieveLiquidContainerTaskProvider = mock(Provider.class);
        _fillLiquidContainerTaskProvider = mock(Provider.class);
        _drinkTaskProvider = mock(Provider.class);
        _personProperties = mock(PersonProperties.class);

        createProvisionTask();
    }
}
