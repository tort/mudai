package com.tort.mudai.task;

import com.google.inject.Provider;
import com.tort.mudai.command.Command;
import com.tort.mudai.command.ExamineItemCommand;
import com.tort.mudai.command.InventoryCommand;
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
    private Provider<RetrieveLiquidContainerTask> _retrieveLiquidContainerTaskProvider;
    private Provider<FillLiquidContainerTask> _fillLiquidContainerTaskProvider;

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

        verify(_eventDistributor).subscribe(isA(RetrieveLiquidContainerTask.class));
    }

    public void waterContainerPresent() {
        _provisionTask.inventory(new String[]{WATER_CONTAINER});
        final Command command = _provisionTask.pulse();

        assertTrue(command instanceof ExamineItemCommand);
    }

    public void containerFull() {
        _provisionTask.waterContainerFull();

        verifyTaskSubscribed(DrinkTask.class);
    }

    private void verifyTaskSubscribed(final Class<? extends AbstractTask> taskClass) {
        verify(_eventDistributor).subscribe(isA(taskClass));
    }

    public void containerAlmostEmpty() {
        _provisionTask.waterContainerAlmostEmpty();

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

        _provisionTask.pulse();

        verifyTaskSubscribed(DrinkTask.class);
    }

    private FillLiquidContainerTask createFillWaterContainerTaskJustTerminated() {
        //TODO generalize creating terminated task
        final FillLiquidContainerTask task = mock(FillLiquidContainerTask.class);
        when(task.status()).thenReturn(Task.Status.TERMINATED);
        return task;
    }

    private RetrieveLiquidContainerTask createBuyWaterContainerTaskJustTerminated() {
        //TODO generalize creating terminated task
        final RetrieveLiquidContainerTask task = mock(RetrieveLiquidContainerTask.class);
        when(task.status()).thenReturn(Task.Status.TERMINATED);
        return task;
    }

    private void createProvisionTask() {
        final RetrieveLiquidContainerTask buyLiquidTask = createBuyWaterContainerTaskJustTerminated();
        when(_retrieveLiquidContainerTaskProvider.get()).thenReturn(buyLiquidTask);
        final FillLiquidContainerTask fillLiquidTask = createFillWaterContainerTaskJustTerminated();
        when(_fillLiquidContainerTaskProvider.get()).thenReturn(fillLiquidTask);

        _eventDistributor = mock(EventDistributor.class);
        _provisionTask = new ProvisionTask(_eventDistributor, _retrieveLiquidContainerTaskProvider, _fillLiquidContainerTaskProvider, WATER_CONTAINER);
    }

    @SuppressWarnings({"unchecked"})
    @BeforeMethod
    protected void setUp() throws Exception {
        _retrieveLiquidContainerTaskProvider = mock(Provider.class);
        _fillLiquidContainerTaskProvider = mock(Provider.class);

        createProvisionTask();
    }
}
