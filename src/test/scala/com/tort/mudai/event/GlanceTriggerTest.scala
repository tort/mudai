package com.tort.mudai.event

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

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

  val buggedDesc = "\u001B[1;36mБольшой туннель\u001B[0;37m\n" +
    "  Господи, ну и сквозняк! Спереди дует, сзади поддувает, снизу что-то потрескивает,\n\n" +
    "сверху капает и сыпется за шиворот... На боках уже живого места не осталось от этих \n\n" +
    "выступов, а они тоже не из лебяжьего пуха. А если в бой? А тут и мечом не размахнешься,\n\n" +
    "и посохом за потолок зацепишься. Безобразие...\n\n" +
    "\n" +
    "\u001B[0;36m[ Exits: n s ]\u001B[0;37m\n" +
    "\u001B[1;33mТруп лягушки лежит здесь.\n" +
    "Труп большого москита лежит здесь.\n" +
    "\u001B[1;31m(летит) Большой москит летает тут.\n" +
    "\u001B[0;0m\n" +
    "\u001B[0;32m185H\u001B[0;37m \u001B[0;32m142M\u001B[0;37m 57051о Зауч:0 10L 1805G Вых:СЮ> "

  val onWalk = "Вы поплелись на восток.\n" +
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

  val darkRoomSample = "Вы поплелись вниз." +
    "\nСлишком темно..." +
    "\n" +
    "\n\u001B[0;32m90H\u001B[0;37m \u001B[0;32m107M\u001B[0;37m 4838о Зауч:0 5L 83G Вых:ЮЗ^> "

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

  val ontheshoreSample = "\u001B[1;36mНа берегу\u001B[0;37m" +
    "\n   Невысокий поросший жидким кустарником берег изрезан следами от пристающих" +
    "\n\nлодок. Мелкая галька шуршит под ногами, не позволяя бесшумно пересечь открытое" +
    "\n\nместо.\n\n" +
    "\n\u001B[0;36m[ Exits: S ]\u001B[0;37m" +
    "\n\u001B[1;37mСнежный ковер лежит у вас под ногами.\u001B[0;37m" +
    "\n\u001B[1;33mЛужица простокваши разлита у ваших ног." +
    "\n\u001B[1;31mМолодой лодочник весело смотрит вокруг." +
    "\nПожилой широкоплечий крестьянин в добротной одежде прохаживается тут." +
    "\n\u001B[0;0m" +
    "\n\u001B[0;32m90H\u001B[0;37m \u001B[0;32m108M\u001B[0;37m 8861о Зауч:0 5L 0G Вых:Ю> "

  val icyRoomSample = "\u001B[1;36mМелководье\u001B[0;37m" +
    "\n  Ты входишь в воду по колено, мелкие водоросли щекочут твои ноги." +
    "\n" +
    "\n\u001B[0;36m[ Exits: n s ]\u001B[0;37m" +
    "\n\u001B[1;34mУ вас под ногами толстый лед.\u001B[0;37m" +
    "\n\u001B[1;33m\u001B[1;31m\u001B[0;0m" +
    "\n\u001B[0;32m90H\u001B[0;37m \u001B[0;32m108M\u001B[0;37m 4315о Зауч:0 5L 83G Вых:СЮ> "

  val descWithTab = "\u001B[1;36mДорога через болото\u001B[0;37m\n" +
    "   Закончившийся на западе ельник помахивает Вам вслед тяжелыми лапами елей.\n" +
    "А на востоке простирается самое настоящее болото, с зыбунами и топями.\n" +
    "Поэтому следует двигаться очень осторожно, ни в коем случае не сворачивать\n" +
    "с протоптанной людьми тропинки.\n" +
    "\n" +
    "\u001B[0;36m[ Exits: n e s w ]\u001B[0;37m\n" +
    "\u001B[1;30mВы просто увязаете в грязи.\u001B[0;37m\n" +
    "\u001B[1;33m\u001B[1;31mБлагородный олень с удивительными золотыми рогами кормится здесь.\n" +
    "\u001B[0;0m\n" +
    "\u001B[0;32m483H\u001B[0;37m \u001B[0;32m244M\u001B[0;37m 10416398о Зауч:0 27L 197G Вых:СВЮЗ> "

  ignore("direction, objects, mobs extraction") {
    val trigger = new GlanceTrigger
    val event = trigger.fireEvent(onWalk)

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

  test("matchLocationWithoutAnyone") {
    val matches = new GlanceTrigger().matches(locationWithoutAnyone)

    matches should be(true)
  }

  test("match on walk") {
    val matches = new GlanceTrigger().matches(onWalk)

    matches should be(true)
  }

  test("match fight") {
    val matches = new GlanceTrigger().matches(fight)

    matches should be(true)
  }

  test("mobs extraction when fight") {
    new GlanceTrigger().fireEvent(input).roomSnapshot.mobs.toSet should equal(Set("(летит) Комар жужжит здесь.", "Таракан быстро пробежал здесь."))
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

  test("on the shore sample") {
    new GlanceTrigger().matches(ontheshoreSample) should be(true)
  }

  test("match dark rooms") {
    new GlanceTrigger().matches(darkRoomSample) should be(true)
  }

  test("room with ice") {
    new GlanceTrigger().matches(icyRoomSample) should be(true)
  }

  test("buggy desc") {
    new GlanceTrigger().matches(descWithTab) should be(true)
  }

  test("desc with only 2 leading spaces") {
    new GlanceTrigger().fireEvent(buggedDesc).roomSnapshot.desc should startWith("Господи")
  }
}

class DisarmTriggerTest extends FunSuite with ShouldMatchers {
  val sample = "Вы пырнули толстого агафона.\n" +
    "Вы слегка укололи толстого агафона.\n" +
    "Толстый агафон ловко выбил копье молодецкое из ваших рук.\n" +
    "Толстый агафон попытался ударить вас, но не рассчитал и промахнулся.\n" +
    "Толстый агафон попытался ударить вас, но не рассчитал и промахнулся.\n" +
    "\n" +
    "250H 138M 5843о Зауч:0:23 [Веретень:Невредим] [толстый агафон:О.тяжело ранен] > "

  test("match disarm"){
    new DisarmTrigger().matches(sample) should be(true)
  }
}
