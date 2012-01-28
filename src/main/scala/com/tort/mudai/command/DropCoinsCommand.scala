package com.tort.mudai.command

class DropCoinsCommand(amount: Int) extends RenderableCommand {
  def render = "бросить %s кун".format(amount)
}
