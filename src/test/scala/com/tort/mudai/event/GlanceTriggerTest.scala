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

  val noMobsCase = "\u001B[1;36mУ колодца\u001B[0;37m" +
    "\n   Хороша водица, сколько не выпьешь все равно мало. Старый колодец говорят" +
    "\nкопал еще первый основатель деревни. Он лично нашел место, выкопал и соорудил" +
    "\nколодец. С тех пор не иссякает вода в колодце, а попробовать ее приезжают из" +
    "\nдалеких мест." +
    "\n" +
    "\n\u001B[0;36m[ Exits: e ]\u001B[0;37m" +
    "\n\u001B[1;33mКолодец выкопан здесь." +
    "\n\u001B[1;31m\u001B[0;0m" +
    "\n\u001B[0;32m42H\u001B[0;37m \u001B[0;32m92M\u001B[0;37m 816о Зауч:0 2L 0G Вых:В> "

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

  val sampleWithMobs = "\u001B[1;36mКомнаты отдыха\u001B[0;37m" +
    "\n   Хозяин устроил здесь комнаты для отдыха. Любой желающий может остановиться" +
    "\nздесь и передохнуть после дальней дороги. Светлые горницы так и приглашают" +
    "\nпройти в них и растянуться на перине, на пуховой да забыться сном богатырским." +
    "\nЗдесь же можно подождать своих компаньонов перед долгой дорогой, поговорить" +
    "\nо дальнейших планах." +
    "\n\u001B[1;32mСовсем малых, да не обученных так и тянет \u001B[1;33mвойти \u001B[1;32mв \u001B[1;33mшколу\u001B[1;32m.\u001B[0;0m" +
    "\n" +
    "\n\u001B[0;36m[ Exits: d ]\u001B[0;37m" +
    "\n\u001B[1;33mДоска для различных заметок и объявлений прибита тут ..блестит!" +
    "\n\u001B[1;31mХозяйка постоялого двора распоряжается здесь." +
    "\n\u001B[0;0m" +
    "\n\u001B[0;32m42H\u001B[0;37m \u001B[0;32m92M\u001B[0;37m 816о Зауч:0 2L 7G Вых:v> "

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

  ignore("direction, objects, mobs extraction") {
    val trigger = new GlanceTrigger
    val event = trigger.fireEvent(text)

    event.direction should not be null
    event.roomSnapshot.objectsPresent.length should equal(3)
    event.roomSnapshot.mobs.length should equal(1)
  }

  ignore("fight") {
    val trigger = new GlanceTrigger()
    val event = trigger.fireEvent(fight);

    event.roomSnapshot.objectsPresent should have length 0
    event.roomSnapshot.mobs should have length 7
    event.roomSnapshot.title should equal("В избе")
  }

  def mockDistributor = {
    Mockito.mock(classOf[EventDistributor])
  }

  test("matchLocationWithoutAnyone") {
    val matches = new GlanceTrigger().matches(locationWithoutAnyone)

    matches should be(true)
  }

  test("match") {
    val matches = new GlanceTrigger().matches(text)

    matches should be(true)
  }

  test("match fight") {
    val matches = new GlanceTrigger().matches(fight)

    matches should be(true)
  }

  test("move with agro") {
    val trigger = new GlanceTrigger()
    assert(trigger.matches(input))
  }

  test("mobs extraction") {
    val mobs = new GlanceTrigger().fireEvent(sampleWithMobs).roomSnapshot.mobs
    mobs should have size (1)
  }

   test("no mobs case") {
    val mobs = new GlanceTrigger().fireEvent(noMobsCase).roomSnapshot.mobs
    mobs should have size (0)
  }
}

