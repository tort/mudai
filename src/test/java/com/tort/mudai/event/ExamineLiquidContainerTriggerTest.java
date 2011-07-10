package com.tort.mudai.event;

import org.testng.annotations.Test;

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

    @Test
    public void testMatches() throws Exception {
        boolean matches = new ExamineLiquidContainerTrigger(null).matches(FULL_LIQUID_CONT);

        assertTrue(matches);
    }
}
