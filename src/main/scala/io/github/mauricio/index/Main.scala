package io.github.mauricio.index

import io.github.mauricio.index.netty.Initializer
import io.github.mauricio.index.server.ThreadedServer

object Main {

  def main(args: Array[String]): Unit = {
    println(s"Server parameters are ${args.toList}")

    val index = new Index()
    val operationExecutor = new OperationExecutor(index)
    val server = if (args.length == 0 || args(0) != "threaded") {
      new Initializer(8080, operationExecutor)
    } else {
      new ThreadedServer(8080, operationExecutor)
    }

    server.start()

    while (true) {
      Thread.sleep(10000)
    }
  }

}
