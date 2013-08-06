package com.tort.mudai.event

class DiscoverObstacleTrigger extends EventTrigger[DiscoverObstacleEvent] {
  val Pattern = ("^(?:Закрыто\\..*)|(?:Закрыто \\(([^\n]*)\\)\\..*)").r

  override def matches(text: String) = {
    text.matches(Pattern.toString())
  }

  def fireEvent(text: String) = {
    val Pattern(obstacle) = text

    DiscoverObstacleEvent(obstacle)
  }
}
