package com.tort.mudai.event

class BashTrigger extends EventTrigger[BashEvent]{
  val Pattern = """(?ms).*\u001B\[\d\;\d\dm([^\n]*) завалил[ао]? вас на землю. Поднимайтесь!.*""".r
  val Pattern2 = """(?ms).*Вы полетели на землю от мощного удара ([^\n]*)\..*""".r
  val Pattern3 = """Оглушающий удар дикого быка сбил вас с ног."""

  def matches(text: String) = text.matches(Pattern.toString()) || text.matches(Pattern2.toString())

  def fireEvent(text: String) = {
    println("BASH EVENT FIRED")
    text match {
      case Pattern(basher) => BashEvent(basher, None)
      case Pattern2(basher) => BashEvent(basher, None)
    }
  }
}

case class BashEvent(basher: String, target: Option[String]) extends Event
