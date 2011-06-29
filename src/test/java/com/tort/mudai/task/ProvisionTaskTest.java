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
    private Provider<GoAndBuyWaterContainerTask> _goAndByWaterContainerTaskProvider;
    private Provider<GoAndFillWaterContainerTask> _fillWaterContainerTaskProvider;

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

        verify(_eventDistributor).subscribe(isA(GoAndBuyWaterContainerTask.class));
    }

    public void waterContainerPresent() {
        _provisionTask.inventory(new String[]{WATER_CONTAINER});
        final Command command = _provisionTask.pulse();

        assertWaterContainerPresent(command);
    }

    public void containerFull() {
        _provisionTask.waterContainerFull();

        verifyDrinkTaskSubscribed();
    }

    private void verifyDrinkTaskSubscribed() {
        verify(_eventDistributor).subscribe(isA(DrinkTask.class));
    }

    public void containerAlmostEmpty() {
        _provisionTask.waterContainerAlmostEmpty();

        verify(_eventDistributor).subscribe(isA(GoAndFillWaterContainerTask.class));
    }

    public void afterBuyWaterContainer() {
        createProvisionTask();
        _provisionTask.inventory(new String[]{WATER_CONTAINER});

        final Command command = _provisionTask.pulse();

        assertWaterContainerPresent(command);
    }

    public void afterFillWaterContainer() {
        createProvisionTask();

        _provisionTask.pulse();

        verifyDrinkTaskSubscribed();
    }

    private GoAndFillWaterContainerTask createFillWaterContainerTaskJustTerminated() {
        //TODO generalize creating terminated task
        final GoAndFillWaterContainerTask task = mock(GoAndFillWaterContainerTask.class);
        when(task.status()).thenReturn(Task.Status.TERMINATED);
        return task;
    }

    private GoAndBuyWaterContainerTask createBuyWaterContainerTaskJustTerminated() {
        //TODO generalize creating terminated task
        final GoAndBuyWaterContainerTask task = mock(GoAndBuyWaterContainerTask.class);
        when(task.status()).thenReturn(Task.Status.TERMINATED);
        return task;
    }

    private void createProvisionTask() {
        final GoAndBuyWaterContainerTask buyWaterTask = createBuyWaterContainerTaskJustTerminated();
        when(_goAndByWaterContainerTaskProvider.get()).thenReturn(buyWaterTask);

        _eventDistributor = mock(EventDistributor.class);
        _provisionTask = new ProvisionTask(_eventDistributor, _goAndByWaterContainerTaskProvider, WATER_CONTAINER);
    }

    private void assertWaterContainerPresent(final Command command) {
        assertTrue(command instanceof ExamineItemCommand);
    }

    @SuppressWarnings({"unchecked"})
    @BeforeMethod
    protected void setUp() throws Exception {
        _goAndByWaterContainerTaskProvider = mock(Provider.class);
        _fillWaterContainerTaskProvider = mock(Provider.class);
        createProvisionTask();
    }
}
