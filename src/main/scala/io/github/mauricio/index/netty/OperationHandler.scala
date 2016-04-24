package io.github.mauricio.index.netty

import java.util.concurrent.Executors

import io.github.mauricio.index.util.{Constants, DaemonThreadFactory}
import io.github.mauricio.index.{Fail, Ok, OperationExecutor}
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.util.ReferenceCountUtil

import scala.util.{Failure, Success}

@Sharable
class OperationHandler(operationExecutor: OperationExecutor) extends SimpleChannelInboundHandler[ByteBuf] {

  private val pool = Executors.newFixedThreadPool(4, DaemonThreadFactory)

  override def channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf): Unit = {

    ReferenceCountUtil.retain(msg)

    pool.submit(new Runnable {
      override def run(): Unit = {

        try {
          val result = operationExecutor.execute(msg) match {
            case Success(Ok) => Constants.OkResponse
            case Success(Fail) => Constants.FailResponse
            case Failure(e) => Constants.ErrorResponse
          }

          ctx.writeAndFlush(result)
        } finally {
          ReferenceCountUtil.release(msg)
        }

      }
    })
  }

  def stop(): Unit =
    pool.shutdown()

}
