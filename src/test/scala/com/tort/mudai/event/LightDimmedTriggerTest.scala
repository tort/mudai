package com.tort.mudai.event

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class LightDimmedTriggerTest extends FunSuite with ShouldMatchers {
  val sample = "Минул час.\nСлабый ветер.\nВаша свечка замерцала и начала угасать.\n\n\n122H 119M 5259о Зауч:2:13 [Веретень:Невредим] [ерш:Ранен] > "
  test("light dimmed matches") {
    new LightDimmedTrigger().matches(sample) should be(true)
  }
}
