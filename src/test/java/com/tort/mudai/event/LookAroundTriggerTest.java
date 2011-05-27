package com.tort.mudai.event;

import com.tort.mudai.Handler;
import com.tort.mudai.task.EventDistributor;
import org.testng.annotations.Test;

import java.util.regex.Matcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class LookAroundTriggerTest {
    private String text = "Вы поплелись на восток.\n" +
            "\u001B[1;36mЗаводь\u001B[0;37m\n" +
            "   Низинка у реки, которая при половодье разливается на многие версты.\n" +
            "\n" +
            "\u001B[1;33mТруп выпи лежит здесь.\n" +
            "Труп полоза лежит здесь.\n" +
            "Труп аиста лежит здесь.\n" +
            "\u001B[1;31mУж проползает мимо Вас.\n" +
            "\u001B[0;37m\n" +
            "\u001B[0;32m28H\u001B[0;37m \u001B[0;32m85M\u001B[0;37m 1499о Зауч:0 Вых:ВЗ> ЪЫ";

    private String locationWithoutAnyone = "\u001B[1;36mБазарная площадь\u001B[0;37m\n" +
            "   Центр городища, получивший свое название по поводу проводимых на нем\n" +
            "празднеств и гуляний. Немноголюдная в будни, она оправдывает его по осени,\n" +
            "после сбора урожая, когда весь рабочий да и гулящий люд не только с городища,\n" +
            "да и с окрестных слобод и селений собирается сюда людей посмотреть, себя \n" +
            "показать, потешить буйно головушку зеленым вином да завозными побасенками.\n" +
            "\n" +
            "\u001B[1;33m\u001B[1;31m\u001B[0;37m\n" +
            "\u001B[0;32m28H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 1499о Зауч:0 Вых:СВЮЗ> ЪЫ";

    public void testObjects() {
        Matcher matcher = LookAroundTrigger.PATTERN.matcher(text);
        matcher.find();

        String group = matcher.group(2);
        String[] objects = group.split("\n");

        assertEquals(objects.length, 4);
    }

    public void matchLocationWithoutAnyone(){
        boolean matches = new LookAroundTrigger(null).matches(locationWithoutAnyone);

        assertTrue(matches);
    }

    public void testMatch() {
        boolean matches = new LookAroundTrigger(null).matches(text);

        assertTrue(matches);
    }
}
