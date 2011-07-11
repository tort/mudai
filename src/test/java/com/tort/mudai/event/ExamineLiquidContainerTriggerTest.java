package com.tort.mudai.event;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ExamineLiquidContainerTriggerTest {
    private static final String FULL_LIQUID_CONT = "Вы видите слегка помятую флягу из тонкого\n" +
            "железа.\n" +
            "\n" +
            "Сoстояние: средне.\n" +
            "Наполнена прозрачной жидкостью.\n" +
            "\n" +
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m86M\u001B[0;37m 1499о Зауч:0 Вых:v> ";

    private static final String HALF_FULL_LIQUID_CONT = "Вы видите слегка помятую флягу из тонкого\n" +
            "железа.\n" +
            "\n" +
            "Сoстояние: средне.\n" +
            "Наполнена примерно наполовину прозрачной жидкостью.\n" +
            "\n" +
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 1499о Зауч:0 Вых:v> ";

    private static final String EMPTY_LIQUID_CONT = "Вы видите слегка помятую флягу из тонкого\n"+
            "железа.\n"+
            "\n"+
            "Сoстояние: средне.\n"+
            "Пусто.\n"+
            "\n"+
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 1499о Зауч:0 Вых:v> ";

    private static final String FULL_FLASK = "Это емкость для жидкости.\n" +
            "Сoстояние: идеально.\n" +
            "Наполнена бордовой жидкостью.\n" +
            "\n" +
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 1499о Зауч:0 Вых:С^> ";

    private static final String HALF_FULL_FLASK = "Это емкость для жидкости.\n" +
            "Сoстояние: идеально.\n" +
            "Наполнена примерно наполовину бордовой жидкостью.\n" +
            "\n" +
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 1499о Зауч:0 Вых:С^> ";

    private static final String ALMOST_EMPTY_FLASK = "Это емкость для жидкости.\n" +
            "Сoстояние: идеально.\n" +
            "Наполнена меньше, чем наполовину бордовой жидкостью.\n" +
            "\n" +
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 1499о Зауч:0 Вых:С^> ";

    private static final String EMPTY_FLASK = "Это емкость для жидкости.\n" +
            "Сoстояние: идеально.\n" +
            "Пусто.\n" +
            "\n" +
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 1499о Зауч:0 Вых:С^> ";

    @Test(dataProvider = "samples")
    public void matches(String text) throws Exception {
        boolean matches = new ExamineLiquidContainerTrigger(null).matches(text);

        assertTrue(matches);
    }

    @Test(dataProvider = "emptyLiquidContainerSamples")
    public void empty(String sample){
        final ExamineLiquidContainerTrigger trigger = new ExamineLiquidContainerTrigger(null);

        final ExamineLiquidContainerEvent event = trigger.fireEvent(sample);

        assertNotNull(event);
        assertEquals(event.getState(), LiquidContainer.State.EMPTY);
    }

    @DataProvider(name = "samples")
    public Object[][] liquidContainerSamples(){
        return new Object[][]{{FULL_LIQUID_CONT},
                {HALF_FULL_LIQUID_CONT},
                {EMPTY_LIQUID_CONT},
                {FULL_FLASK},
                {HALF_FULL_FLASK},
                {ALMOST_EMPTY_FLASK},
                {EMPTY_FLASK}};
    }

    @DataProvider(name = "emptyLiquidContainerSamples")
    public Object[][] emptyLiquidContainerSamples(){
        return new Object[][]{
                {EMPTY_FLASK}, {EMPTY_LIQUID_CONT}
        };
    }
}
