package com.tort.mudai.event

class DiscoverObstacleTrigger extends EventTrigger[DiscoverObstacleEvent] {
  val Pattern = ("(?ms).*(?:Закрыто\\.).*").r

  override def matches(text: String) = {
    text.matches(Pattern.toString())
  }

  def fireEvent(text: String) = {
    DiscoverObstacleEvent("дверь")
  }
}
