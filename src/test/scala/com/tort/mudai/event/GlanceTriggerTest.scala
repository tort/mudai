package com.tort.mudai.event

import com.tort.mudai.task.EventDistributor
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito

class GlanceTriggerTest extends FunSuite with ShouldMatchers {
  val input = "Вы поплелись на запад.\n" +
    "\u001B[1;36mУ печи\u001B[0;37m\n" +
    "   Большая русская печь, сложенная из каленого кирпича, стоит в избе и светит\n" +
    "обмазанными мелом боками. Русская земля богата, но и крепка студеными морозами, \n" +
    "и только на печи можно отогреться.\n" +
    "\n" +
    "\u001B[0;36m[ Exits: n (e) w ]\u001B[0;37m\n" +
    "\u001B[1;33m\u001B[1;31m(летит) Комар жужжит здесь.\n" +
    "Таракан быстро пробежал здесь.\n" +
    "\u001B[0;37m\u001B[0;31mКомар попытался ужалить Вас, но промахнулся.\n" +
    "\u001B[0;37m\n" +
    "\u001B[0;32m40H\u001B[0;37m \u001B[0;32m90M\u001B[0;37m 193о Зауч:0 \u001B[0;32m[Веретень:Невредим]\u001B[0;37m \u001B[0;32m[комар:Невредим]\u001B[0;37m > ЪЫ"

  val text = "Вы поплелись на восток.\n" +
    "\u001B[1;36mЗаводь\u001B[0;37m\n" +
    "   Низинка у реки, которая при половодье разливается на многие версты.\n" +
    "\n" +
    "\u001B[0;36m[ Exits: (e) s ]\u001B[0;37m\n" +
    "\u001B[1;33mТруп выпи лежит здесь.\n" +
    "Труп полоза лежит здесь.\n" +
    "Труп аиста лежит здесь.\n" +
    "\u001B[1;31mУж проползает мимо Вас.\n" +
    "\u001B[0;37m\n" +
    "\u001B[0;32m28H\u001B[0;37m \u001B[0;32m85M\u001B[0;37m 1499о Зауч:0 Вых:ВЗ> ЪЫ"

  val locationWithoutAnyone = "\u001B[1;36mБазарная площадь\u001B[0;37m\n" +
    "   Центр городища, получивший свое название по поводу проводимых на нем\n" +
    "празднеств и гуляний. Немноголюдная в будни, она оправдывает его по осени,\n" +
    "после сбора урожая, когда весь рабочий да и гулящий люд не только с городища,\n" +
    "да и с окрестных слобод и селений собирается сюда людей посмотреть, себя \n" +
    "показать, потешить буйно головушку зеленым вином да завозными побасенками.\n" +
    "\n" +
    "\u001B[0;36m[ Exits: e s ]\u001B[0;37m\n" +
    "\u001B[1;33m\u001B[1;31m\u001B[0;37m\n" +
    "\u001B[0;32m28H\u001B[0;37m \u001B[0;32m88M\u001B[0;37m 1499о Зауч:0 Вых:СВЮЗ> ЪЫ"

  val fight = "\u001B[1;36mВ избе\u001B[0;37m\n" +
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
    "\u001B[0;32m40H\u001B[0;37m \u001B[0;32m93M\u001B[0;37m 134о Зауч:0 \u001B[0;32m[Веретень:Невредим]\u001B[0;37m \u001B[1;32m[комар:Легко ранен]\u001B[0;37m > ЪЫ"

  test("direction, objects, mobs extraction") {
    val trigger = new GlanceTrigger(mockDistributor)
    val event = trigger.fireEvent(text)

    event.direction should not be null
    event.roomSnapshot.objectsPresent.length should equal(3)
    event.roomSnapshot.mobs.length should equal(1)
  }

  test("fight") {
    val trigger = new GlanceTrigger(mockDistributor);
    val event = trigger.fireEvent(fight);

    event.roomSnapshot.objectsPresent should have length 0
    event.roomSnapshot.mobs should have length 7
    event.roomSnapshot.title should equal("В избе")
  }

  def mockDistributor = {
    Mockito.mock(classOf[EventDistributor])
  }

  test("matchLocationWithoutAnyone") {
    val matches = new GlanceTrigger(null).matches(locationWithoutAnyone)

    matches should be(true)
  }

  test("match") {
    val matches = new GlanceTrigger(null).matches(text)

    matches should be(true)
  }

  test("match fight") {
    val matches = new GlanceTrigger(null).matches(fight)

    matches should be(true)
  }

  test("move with agro") {
    val trigger = new GlanceTrigger(null)
    assert(trigger.matches(input))
  }
}
