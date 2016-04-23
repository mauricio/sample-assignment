package io.github.mauricio.index.server

import java.io.{BufferedInputStream, PrintStream}
import java.net.{Socket, SocketTimeoutException}

import io.github.mauricio.index.util.{Constants, Log}
import io.github.mauricio.index.{Fail, Ok, OperationExecutor}

import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}

object ClientWorker {
  val log = Log.get[ClientWorker]
}

class ClientWorker(socket: Socket, executor: OperationExecutor) extends Runnable {

  import ClientWorker._

  private var isConnected = true

  override def run(): Unit = {

    try {
      val output = new PrintStream(socket.getOutputStream)
      val input = new BufferedInputStream(socket.getInputStream)
      val buffer = new ArrayBuffer[Byte](128)

      while (isConnected && socket.isConnected) {
        try {
          val byte = input.read().toByte

          if (byte == Constants.NewLine) {
            val bytes = buffer.toArray
            buffer.clear()
            executor.execute(bytes) match {
              case Success(Ok) => output.println("OK")
              case Success(Fail) => output.println("FAIL")
              case Failure(e) => output.println("ERROR")
            }
            output.flush()
          } else {
            buffer.append(byte)
          }
        } catch {
          case e: SocketTimeoutException => log.info("Timeout happened while trying to read, moving on")
        }

      }

      if (socket.isConnected) {
        output.flush()
        output.close()
        input.close()
      }

    } catch {
      case e: Exception => log.error("Failed to operate on client socket", e)
    } finally {
      if (socket.isConnected) {
        socket.close()
      }
    }

  }


}
