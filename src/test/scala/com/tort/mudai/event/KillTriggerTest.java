package com.tort.mudai.event;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

@Test
public class KillTriggerTest {
    private static final String SAMPLE = "\u001B[1;33mВы захлестали маленького муравья до смерти. \n" +
            "\u001B[0;37mМаленький муравей мертв, его душа медленно подымается в небеса.\n" +
            "Ваш опыт повысился на 34 очка.\n" +
            "Кровушка стынет в жилах от предсмертного крика маленького муравья.\n" +
            "\n" +
            "\u001B[0;32m25H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 690о Зауч:0 Вых:СВЗ> ";

    public void testMatches() throws Exception {
        final KillTrigger trigger = new KillTrigger(null);

        assertTrue(trigger.matches(SAMPLE));
    }
}
