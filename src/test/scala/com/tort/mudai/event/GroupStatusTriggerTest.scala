package com.tort.mudai.event

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.tort.mudai.mapper.Mob.ShortName

class GroupStatusTriggerTest extends FunSuite with ShouldMatchers {
  val sample = "Ваши последователи:\n" +
    "Персонаж            | Здоровье |Рядом| Аффект | Положение\n" +
    "\u001B[1;34mБатрак              \u001B[0;37m|" +
    "\u001B[0;32m Невредим \u001B[0;37m|" +
    "\u001B[0;32m Да  \u001B[0;37m|" +
    " \u001B[1;31m \u001B[0;32m \u001B[1;36m \u001B[1;33m \u001B[1;34m \u001B[0;33m \u001B[0;37m |" +
    "Стоит          \n" +
    "\n" +
    "\u001B[0;32m298H\u001B[0;37m \u001B[0;32m191M\u001B[0;37m 358578о Зауч:0 18L 448G Вых:СВЗ> "

  test("match group status") {
    new GroupStatusTrigger().matches(sample) should be(true)
  }

  test("group member name extraction") {
    new GroupStatusTrigger().fireEvent(sample).shortName should be("Батрак")
  }
}
