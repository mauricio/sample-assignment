package io.github.mauricio.index.netty

import java.nio.charset.StandardCharsets

import io.github.mauricio.index.util.Constants
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

@Sharable
object StringToByteBufEncoder extends MessageToByteEncoder[String] {

  override def encode(ctx: ChannelHandlerContext, msg: String, out: ByteBuf): Unit = {
    out.writeBytes(msg.getBytes(StandardCharsets.UTF_8))
    out.writeByte(Constants.NewLine)
  }

}
