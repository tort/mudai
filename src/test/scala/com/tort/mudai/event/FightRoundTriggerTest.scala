package com.tort.mudai.event

import org.scalatest.FunSuite
import org.scalatest.matchers.MustMatchers

class FightRoundTriggerTest extends FunSuite with MustMatchers {
  val sample: String = "Комар попытался ужалить вас, но промахнулся." +
      "\nНи за что! Вы сражаетесь за свою жизнь!" +
      "\n" +
      "\n26H 84M 1091о Зауч:0 [Веретень:Невредим] [комар:Невредим] > "

  val assistRoundSample = "Галицкий стражник попытался рубануть старичка, но этот удар прошел мимо.\n" +
    "Удар галицкого стражника прошел мимо старичка.\n" +
    "Галицкий стражник чрезвычайно сильно ударил старичка.\n" +
    "Огненный щит старичка отразил часть удара галицкого стражника в него же.\n" +
    "Старичок зыркнул на вас и проревел : '... простер руку свою к огню.'.\n" +
    "Вы заорали от боли, когда старичок схватил вас горящими руками.\n" +
    "Удар старичка прошел мимо галицкого стражника.\n" +
    "Галицкий стражник сумел избежать удара старичка.\n" +
    "\n335H 213M 8237498о Зауч:1:14 [Веретень:Легко ранен] [галицкий стражник:Легко ранен] [старичок:Тяжело ранен] > "

  val trigger = new FightRoundTrigger

  test("fight round trigger match") {
    trigger.matches(sample) must be(true)
  }

  test("fight round event extraction") {
    val event = trigger.fireEvent(sample)
    event.state must be("Невредим")
  }

  test("fight round trigger do NOT react on assist rounds") {
    trigger.matches(assistRoundSample) must be(false)
  }
}
