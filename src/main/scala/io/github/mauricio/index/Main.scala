package io.github.mauricio.index

import io.github.mauricio.index.server.ThreadedServer

object Main {

  def main(args : Array[String]): Unit = {
    val index = new Index()
    val operationExecutor = new OperationExecutor(index)
    val server = new ThreadedServer(8080, operationExecutor)

    server.start()

    while (true) {
      Thread.sleep(10000)
    }
  }

}
