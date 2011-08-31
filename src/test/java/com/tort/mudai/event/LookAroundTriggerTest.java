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
            "\u001B[0;36m[ Exits: (e) s ]\u001B[0;37m\n" +
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
            "\u001B[0;36m[ Exits: e s ]\u001B[0;37m\n" +
            "\u001B[1;33m\u001B[1;31m\u001B[0;37m\n" +
            "\u001B[0;32m28H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 1499о Зауч:0 Вых:СВЮЗ> ЪЫ";

    private String fight = "\u001B[1;36mВ избе\u001B[0;37m\n" +
            "   Вдоль стен поставлены широкие крепкие лавки, на которых можно сидеть,\n" +
            "а в случае необходимости и прилечь. На сделанной из толстой доски лавке\n" +
            "разместится и здоровый мужик.\n" +
            "\n" +
            "\u001B[0;36m[ Exits: e s ]\u001B[0;37m\n" +
            "\u001B[1;33m\u001B[1;31mБлоха прячется в мусоре.\n" +
            "(летит) Моль летает здесь.\n" +
            "(летит) Муха летает здесь.\n" +
            "Комар сражается c ВАМИ ! \n" +
            "Клоп ползает здесь.\n" +
            "Таракан быстро пробежал здесь.\n" +
            "Клоп ползает здесь.\n" +
            "\u001B[0;37m\n" +
            "\u001B[0;32m40H\u001B[0;37m \u001B[0;32m93M\u001B[0;37m 134о Зауч:0 \u001B[0;32m[Веретень:Невредим]\u001B[0;37m \u001B[1;32m[комар:Легко ранен]\u001B[0;37m > ЪЫ";

    public void testObjects() {
        final LookAroundTrigger trigger = new LookAroundTrigger(mockDistributor());
        final LookAroundEvent event = trigger.fireEvent(text);

        assertEquals(event.getObjects().length, 3);
        assertEquals(event.getMobs().length, 1);
    }

    private EventDistributor mockDistributor() {
        return new EventDistributor(){
            @Override
            public void invoke(final Handler handler) {
            }
        };
    }

    public void matchLocationWithoutAnyone(){
        boolean matches = new LookAroundTrigger(null).matches(locationWithoutAnyone);

        assertTrue(matches);
    }

    public void testMatch() {
        boolean matches = new LookAroundTrigger(null).matches(text);

        assertTrue(matches);
    }

    public void matchFight() {
        boolean matches = new LookAroundTrigger(null).matches(fight);

        assertTrue(matches);
    }
}
