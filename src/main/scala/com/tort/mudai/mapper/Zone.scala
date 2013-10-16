package com.tort.mudai.mapper

import scalaz.Tag

class Zone(val id: String, val name: String)

object Zone {
  trait ZoneName

  def name(n: String) = Tag[String, ZoneName](n)
}
