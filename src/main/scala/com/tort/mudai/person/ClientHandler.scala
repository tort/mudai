package com.tort.mudai.person

import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder
import org.jboss.netty.buffer.ChannelBuffers

class ClientHandler(onRawRead: (String) => Unit, onCloseRemote: () => Unit) extends SimpleChannelUpstreamHandler {
  val markersGA = Seq("""В связи с проблемами перевода фразы ANYKEY нажмите ENTER""", """Пересоединяемся.""")
  var modeGA = false

  override def channelDisconnected(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    onCloseRemote()
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val text = e.getMessage.toString
    if (!modeGA && markersGA.exists(m => text.contains(m))) {
      ctx.getPipeline.addFirst("decoder IAC GA", decoderGA)
      modeGA = true
    }
    onRawRead(text)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    println(e.getCause.getMessage)
    e.getChannel.close()
  }

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent) {
    e match {
      case c: ChannelStateEvent => println(c.toString)
      case _ => None
    }
    super.handleUpstream(ctx, e)
  }

  val decoderGA = new DelimiterBasedFrameDecoder(
    16384,
    ChannelBuffers.wrappedBuffer(Array(255, 249).map(_.toByte))
  )
}
