package com.tort.mudai.event

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

class KillTriggerTest extends FunSuite with ShouldMatchers {
  val Sample = "\n\u001B[1;33mВы смертельно пырнули муху. Не приходя в сознание, муха скончалась." +
    "\n\u001B[0;0mТолстая муха мертва, ее душа медленно подымается в небеса." +
    "\nВаш опыт повысился на 63 очка." +
    "\nКровушка стынет в жилах от предсмертного крика мухи." +
    "\n" +
    "\n\u001B[0;32m41H\u001B[0;37m \u001B[0;32m92M\u001B[0;37m 949о Зауч:0 2L 12G Вых:ВЗ> "

  val magicalDeath = "Дружинник изрубил дикую яблоньку. От дикой яблоньки осталось только кровавое месиво.\n" +
    "Дикая яблонька вспыхнула и рассыпалась в прах.\n" +
    "Ваш опыт повысился на 7000 очков.\n" +
    "Кровушка стынет в жилах от предсмертного крика дикой яблоньки.\n" +
    "\n" +
    "378H 228M 529398о Зауч:0 23L 62G Вых:СВЮЗ> "

  test("kill trigger matches") {
    new KillTrigger().matches(Sample) should be(true)
  }

  test("kill trigger event extraction") {
    val event = new KillTrigger().fireEvent(Sample)
    event.target should be("Толстая муха")
    event.exp should be(63)
  }

  test("magical mob death") {
    new KillTrigger().fireEvent(magicalDeath).target should be("Дикая яблонька")
  }
}
