package com.tort.mudai.event

import com.tort.mudai.RoomSnapshot
import com.tort.mudai.mapper.Direction

case class GlanceEvent(
                        roomSnapshot: RoomSnapshot,
                        direction: Option[Direction]
                        ) extends Event

