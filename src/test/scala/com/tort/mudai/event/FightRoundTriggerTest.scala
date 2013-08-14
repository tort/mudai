package com.tort.mudai.event

import org.scalatest.FunSuite
import org.scalatest.matchers.MustMatchers

class FightRoundTriggerTest extends FunSuite with MustMatchers {
  val sample: String = "Комар попытался ужалить вас, но промахнулся." +
      "\nНи за что! Вы сражаетесь за свою жизнь!" +
      "\n" +
      "\n26H 84M 1091о Зауч:0 [Веретень:Невредим] [комар:Невредим] > "
  val trigger = new FightRoundTrigger

  test("fight round trigger match") {
    trigger.matches(sample) must be(true)
  }

  test("fight round event extraction") {
    val event = trigger.fireEvent(sample)
    event.state must be("Невредим")
  }
}
