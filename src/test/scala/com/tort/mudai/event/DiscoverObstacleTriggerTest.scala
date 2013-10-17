package com.tort.mudai.event

import org.scalatest.FunSuite

class DiscoverObstacleTriggerTest extends FunSuite {
  val input = "Закрыто.\n\n\u001B[0;32m40H\u001B[0;37m \u001B[0;32m92M\u001B[0;37m 441о Зауч:0 Вых:(С)В(Ю)> "

  test("discover obstacle trigger match") {
    val trigger = new DiscoverObstacleTrigger()
    assert(trigger.matches(input))
  }

  test("discover obstacle trigger variables extraction") {
    val trigger = new DiscoverObstacleTrigger()
    val event = trigger.fireEvent(input)
    assert(event.obstacle == null)
  }
}