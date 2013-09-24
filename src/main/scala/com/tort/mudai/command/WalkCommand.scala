package com.tort.mudai.command

import com.tort.mudai.mapper.Direction
import scalaz.@@

case class WalkCommand(direction: String @@ Direction) extends RenderableCommand {
  def render: String = direction
}
