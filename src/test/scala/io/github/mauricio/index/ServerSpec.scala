package io.github.mauricio.index

import java.util.concurrent.atomic.AtomicInteger

import io.github.mauricio.index.server.ClientSocket
import io.github.mauricio.index.util.Constants
import org.specs2.mutable.Specification

object ServerSpec {

  val Ports = new AtomicInteger(20000)

}

abstract class ServerSpec extends Specification {

  def createServer(port: Int, operationExecutor: OperationExecutor): Server

  def withServer[A](f: (ClientSocket) => A): A = {
    val index = new Index()
    val operationExecutor = new OperationExecutor(index)
    val port = ServerSpec.Ports.incrementAndGet()
    val server = createServer(port, operationExecutor)
    server.start()
    val client = new ClientSocket(port)

    try {
      f(client)
    } finally {
      client.close()
      server.stop()
    }
  }

  "threaded server" >> {

    "server acceps and answers requests from clients" >> {

      withServer {
        client =>
          client.index("java") === Constants.OkResponse
          client.query("java") === Constants.OkResponse
          client.query("scala") === Constants.FailResponse
      }
    }

    "server allows many clients to connect and communicate" >> {

      withServer {
        client =>
          val client2 = new ClientSocket(client.port)
          val client3 = new ClientSocket(client.port)
          val client4 = new ClientSocket(client.port)

          try {

            client.index("java") === Constants.OkResponse
            client2.query("java") === Constants.OkResponse

            client3.index("scala", Set("java")) === Constants.OkResponse
            client4.remove("java") === Constants.FailResponse
            client2.remove("scala") === Constants.OkResponse
            client2.remove("unknown") === Constants.OkResponse
          } finally {
            client2.close()
            client3.close()
            client4.close()
          }
      }

    }

    "server sends error when client sends bad input" >> {

      withServer {
        client =>
          client.write("bad message!") === Constants.ErrorResponse
      }
    }

  }

}
