package io.github.mauricio.index.netty

import io.github.mauricio.index.{OperationExecutor, Server, ServerSpec}

class NettyServerSpec extends ServerSpec {

  override def createServer(port: Int, operationExecutor: OperationExecutor): Server =
    new Initializer(port, operationExecutor)

}
