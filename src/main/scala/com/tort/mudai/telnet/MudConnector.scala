package com.tort.mudai.telnet

import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import java.util.concurrent.Executors
import com.tort.mudai.person.{Disconnect, RawRead, TelnetPipelineFactory}
import java.net.InetSocketAddress
import akka.actor.ActorRef

class MudConnector {
  def connect(adapter: ActorRef) = {
    val client = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()))
    client.setPipeline(new TelnetPipelineFactory((msg: String) => adapter ! RawRead(msg), () => adapter ! Disconnect).getPipeline)
    client.connect(new InetSocketAddress("178.21.10.107", 4000)).awaitUninterruptibly()
  }
}
