package io.github.mauricio.index.server

import io.github.mauricio.index.{OperationExecutor, Server, ServerSpec}

class ThreadedServerSpec extends ServerSpec {

  override def createServer(port: Int, operationExecutor: OperationExecutor): Server =
    new ThreadedServer(port, operationExecutor)

}
