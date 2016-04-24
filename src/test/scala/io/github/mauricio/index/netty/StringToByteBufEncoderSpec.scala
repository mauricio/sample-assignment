package io.github.mauricio.index.netty

import java.nio.charset.StandardCharsets

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.embedded.EmbeddedChannel
import org.specs2.mutable.Specification

class StringToByteBufEncoderSpec extends Specification {

  "encoder" >> {

    "encodes the strings into byte bufs" >> {
      val channel = new EmbeddedChannel(StringToByteBufEncoder)
      channel.writeOutbound("OK")

      val buffer = channel.readOutbound().asInstanceOf[ByteBuf]

      buffer === Unpooled.wrappedBuffer("OK\n".getBytes(StandardCharsets.UTF_8))
    }

  }

}
