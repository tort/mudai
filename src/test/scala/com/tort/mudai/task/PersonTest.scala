package com.tort.mudai.task

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.google.inject.Guice
import com.tort.mudai.MudaiModule

class PersonTest extends FunSuite with ShouldMatchers {
  val injector = Guice.createInjector(new MudaiModule)
  val person = injector.getInstance(classOf[Person])

  test("save stat") {
    person.viewStat("test stat")
  }
}
