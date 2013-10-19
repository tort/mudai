package com.tort.mudai.mapper

import scalaz.{@@, Tag}
import com.tort.mudai.mapper.Zone.ZoneName

class Zone(val id: String, val name: String @@ ZoneName)

object Zone {
  trait ZoneName

  def name(n: String) = Tag[String, ZoneName](n)
}
