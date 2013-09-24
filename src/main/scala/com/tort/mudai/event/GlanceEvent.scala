package com.tort.mudai.event

import com.tort.mudai.RoomSnapshot
import com.tort.mudai.mapper.Direction
import scalaz.@@

case class GlanceEvent(
                        roomSnapshot: RoomSnapshot,
                        direction: Option[String @@ Direction]
                        ) extends Event

