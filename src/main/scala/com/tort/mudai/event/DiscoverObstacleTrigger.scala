package com.tort.mudai.event

class DiscoverObstacleTrigger extends EventTrigger[DiscoverObstacleEvent] {
  val Pattern = ("(?ms).*Закрыто[^\n]*\\..*").r
  val NamedObstaclePattern = ("(?ms).*Закрыто \\(([^\n]*)\\)\\..*").r

  override def matches(text: String) = {
    text.matches(Pattern.toString())
  }

  def fireEvent(text: String) = {
    text match {
      case NamedObstaclePattern(obstacle) => DiscoverObstacleEvent(obstacle)
      case _ => DiscoverObstacleEvent("дверь")
    }
  }
}
