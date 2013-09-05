package com.tort.mudai.event

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class TargetAssistedTriggerTest extends FunSuite with ShouldMatchers {
  val sample = "\nБобер вступил в битву на стороне бобра." +
    "\n\u001B[0;31mБобер попытался укусить вас, но поймал зубами лишь воздух." +
    "\n\u001B[0;0m" +
    "\n\u001B[0;32m202H\u001B[0;37m \u001B[0;32m133M\u001B[0;37m 91686о Зауч:- \u001B[0;32m[Веретень:Невредим]\u001B[0;37m \u001B[0;32m[бобер:Слегка ранен]\u001B[0;37m > "

  test("target assisted trigger matches") {
    new TargetAssistedTrigger().matches(sample.toString) should be(true)
  }

  test("assister extraction") {
    new TargetAssistedTrigger().fireEvent(sample).target should be("Бобер")
  }
}
