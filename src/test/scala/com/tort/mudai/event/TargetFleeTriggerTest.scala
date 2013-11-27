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

  val sample2 = "Удар белокрылого дятла прошел мимо галицкого стражника.\n" +
    "Галицкий стражник чрезвычайно сильно ударил белокрылого дятла.\n" +
    "Галицкий стражник чрезвычайно сильно ударил белокрылого дятла.\n" +
    "Белокрылый дятел запаниковал и пытался сбежать!\n" +
    "Белокрылый дятел сбежал на восток.\n" +
    "\n" +
    "418H 226M 79751о Зауч:0 24L 4985G Вых:ВЮЗ> "

  test("flee trigger matches") {
    new TargetFleeTrigger().matches(sample2) should be(true)
  }
}
