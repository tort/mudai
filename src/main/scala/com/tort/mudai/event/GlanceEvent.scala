package com.tort.mudai.event

import com.tort.mudai.RoomSnapshot

case class GlanceEvent(
                        roomSnapshot: RoomSnapshot,
                        direction: Option[String]
                        ) extends Event

