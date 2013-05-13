package com.tort.mudai.person

import org.jboss.netty.channel._
import org.jboss.netty.channel.Channels._
import org.jboss.netty.handler.codec.frame.{Delimiters, DelimiterBasedFrameDecoder}
import org.jboss.netty.handler.codec.string.{StringEncoder, StringDecoder}
import org.jboss.netty.buffer.ChannelBuffers

class TelnetPipelineFactory(onRawRead: (String) => Unit, onCloseRemote: () => Unit) extends ChannelPipelineFactory {
  def getPipeline = {
    pipeline(
      new StringDecoder,
      new StringEncoder,
      new ClientHandler(onRawRead, onCloseRemote)
    )
  }
}
