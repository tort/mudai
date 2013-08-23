package com.tort.mudai.event

import org.scalatest.FunSuite

class DiscoverObstacleTriggerTest extends FunSuite {
  val input = "Закрыто.\n\n\u001B[0;32m40H\u001B[0;37m \u001B[0;32m92M\u001B[0;37m 441о Зауч:0 Вых:(С)В(Ю)> "

  test("pattern match etalon input") {
    val trigger = new DiscoverObstacleTrigger()
    assert(trigger.matches(input))
  }

  test("variables extracted properly") {
    val trigger = new DiscoverObstacleTrigger()
    val event = trigger.fireEvent(input)
    assert(event.obstacle == null)
  }
}