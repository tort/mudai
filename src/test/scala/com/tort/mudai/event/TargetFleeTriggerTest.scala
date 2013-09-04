package com.tort.mudai.event

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class TargetFleeTriggerTest extends FunSuite with ShouldMatchers {
  val sample = "Заяц-русак попытался ткнуть вас, но промахнулся." +
    "\nВы огрели зайца-русака." +
    "\nЗаяц-русак запаниковал и пытался сбежать!" +
    "\nЗаяц-русак сбежал на север." +
    "\n" +
    "\n186H 56M 102388о Зауч:0 11L 137G Вых:СВЮ> "

  test("flee trigger matches") {
    new TargetFleeTrigger().matches(sample) should be(true)
  }
}
