package io.github.mauricio.index.server

import java.net.{InetSocketAddress, ServerSocket, SocketTimeoutException}
import java.util.concurrent.Executors

import io.github.mauricio.index.util.{Constants, DaemonThreadFactory, Log}
import io.github.mauricio.index.{OperationExecutor, Server}

object ThreadedServer {
  val log = Log.get[ThreadedServer]
}

class ThreadedServer(
                      port: Int,
                      executor: OperationExecutor,
                      acceptTimeout: Int = Constants.ReadTimeout,
                      readTimeout: Int = Constants.ReadTimeout
                    ) extends Server {

  import ThreadedServer._

  private val pool = Executors.newCachedThreadPool(DaemonThreadFactory)
  private val serverSocket = new ServerSocket()
  serverSocket.setSoTimeout(acceptTimeout)
  private var isRunning = false

  def start(): Unit = {

    log.info("Starting threaded server")

    serverSocket.bind(new InetSocketAddress(Constants.DefaultHost, port))

    isRunning = true

    pool.submit(new Runnable {
      override def run(): Unit = {
        try {
          while (isRunning) {
            try {
              val client = serverSocket.accept()
              client.setSoTimeout(readTimeout)
              pool.submit(new ClientWorker(client, executor))
            } catch {
              case e: SocketTimeoutException =>
                log.info("Timeout while accepting, moving on")
            }
          }
          serverSocket.close()
          pool.shutdown()
        } catch {
          case e: Exception =>
            log.error("Failed on server socket loop", e)
        }
      }
    })

  }

  def stop(): Unit = {
    log.info("Stopping threaded server")
    isRunning = false
  }


}
