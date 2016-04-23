package io.github.mauricio.index.server

import java.util.concurrent.atomic.AtomicInteger

import io.github.mauricio.index.{Index, OperationExecutor}
import org.specs2.mutable.Specification

object ThreadedServerSpec {

  val Ports = new AtomicInteger(20000)

  val OkResponse = "OK"
  val FailResponse = "FAIL"
  val ErrorResponse = "ERROR"

}

class ThreadedServerSpec extends Specification {

  def withServer[A](f: (ThreadedServer, ClientSocket) => A): A = {
    val index = new Index()
    val operationExecutor = new OperationExecutor(index)
    val port = ThreadedServerSpec.Ports.incrementAndGet()
    val server = new ThreadedServer(port, operationExecutor)
    server.start()
    val client = new ClientSocket(port)

    try {
      f(server, client)
    } finally {
      client.close()
      server.stop()
    }
  }

  "threaded server" >> {

    "server acceps and answers requests from clients" >> {

      withServer {
        (server, client) =>
          client.index("java") === ThreadedServerSpec.OkResponse
          client.query("java") === ThreadedServerSpec.OkResponse
          client.query("scala") === ThreadedServerSpec.FailResponse
      }
    }

    "server allows many clients to connect and communicate" >> {

      withServer {
        (server, client) =>
          val client2 = new ClientSocket(client.port)
          val client3 = new ClientSocket(client.port)
          val client4 = new ClientSocket(client.port)

          try {

            client.index("java") === ThreadedServerSpec.OkResponse
            client2.query("java") === ThreadedServerSpec.OkResponse

            client3.index("scala", Set("java")) === ThreadedServerSpec.OkResponse
            client4.remove("java") === ThreadedServerSpec.FailResponse
            client2.remove("scala") === ThreadedServerSpec.OkResponse
            client2.remove("unknown") === ThreadedServerSpec.OkResponse
          } finally {
            client2.close()
            client3.close()
            client4.close()
          }
      }

    }

    "server sends error when client sends bad input" >> {

      withServer {
        (server, client) =>
          client.write("bad message!") === ThreadedServerSpec.ErrorResponse
      }
    }

  }

}
