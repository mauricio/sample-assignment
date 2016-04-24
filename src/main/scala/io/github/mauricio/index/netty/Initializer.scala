package io.github.mauricio.index.netty

import io.github.mauricio.index.util.{Constants, Log}
import io.github.mauricio.index.{OperationExecutor, Server}
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, ChannelInitializer, ChannelOption}
import io.netty.handler.codec.DelimiterBasedFrameDecoder

object Initializer {
  val log = Log.get[Initializer]
}

class Initializer(port: Int, operationExecutor: OperationExecutor)
  extends ChannelInitializer[SocketChannel]
    with Server {

  import Initializer.log

  private val bossGroup = new NioEventLoopGroup(1)
  private val workerGroup = new NioEventLoopGroup()

  private val serverBootstrap = new ServerBootstrap()
  serverBootstrap.option(ChannelOption.SO_BACKLOG, java.lang.Integer.valueOf(1024))
  serverBootstrap.group(bossGroup, workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(this)
  private val handler = new OperationHandler(operationExecutor)
  private var serverChannel: Channel = null

  override def initChannel(ch: SocketChannel): Unit = {
    val p = ch.pipeline()

    p.addLast("string-to-buff-encoder", StringToByteBufEncoder)
    p.addLast(
      "delimiter-decoder",
      new DelimiterBasedFrameDecoder(
        Integer.MAX_VALUE,
        Unpooled.wrappedBuffer(Constants.NewLineBytes)
      )
    )
    p.addLast("operation-handler", this.handler)
  }

  def start(): Unit = {
    serverChannel = serverBootstrap.bind(port).sync().channel()

    val thread = new Thread(new Runnable {
      override def run(): Unit = {
        try {
          log.info(s"Starting server ${serverChannel}")
          serverChannel.closeFuture().sync()
        } catch {
          case e: Exception =>
            log.error(s"Server channel failed with ${e.getMessage}", e)
        }
        finally {
          bossGroup.shutdownGracefully()
          workerGroup.shutdownGracefully()
        }
      }
    })

    thread.setDaemon(true)
    thread.start()
  }

  def stop(): Unit = {
    log.info(s"Stopping server ${serverChannel}")
    val channelFuture = serverChannel.close().awaitUninterruptibly()
    log.info(s"Closed server channel ${serverChannel}")
    handler.stop()
  }


}
