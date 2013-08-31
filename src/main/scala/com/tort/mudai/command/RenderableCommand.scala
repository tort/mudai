package com.tort.mudai.command

trait Command

trait RenderableCommand extends Command {
    def render : String
}

