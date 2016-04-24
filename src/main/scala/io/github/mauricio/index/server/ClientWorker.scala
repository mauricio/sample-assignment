package io.github.mauricio.index.server

import java.io.{ByteArrayOutputStream, PrintStream}
import java.net.{Socket, SocketTimeoutException}

import io.github.mauricio.index.util.{Constants, Log}
import io.github.mauricio.index.{Fail, Ok, OperationExecutor}

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
      val input = socket.getInputStream
      val buffer = new ByteArrayOutputStream(128)
      val readBuffer = new Array[Byte](128)

      while (isConnected && socket.isConnected) {
        try {
          val readUntil = input.read(readBuffer)

          if (!(readUntil == -1)) {
            var index = 0

            while (index < readUntil) {

              val byte = readBuffer(index)

              if (byte == Constants.NewLine) {
                val bytes = buffer.toByteArray
                buffer.reset()
                executor.execute(bytes) match {
                  case Success(Ok) => output.println("OK")
                  case Success(Fail) => output.println("FAIL")
                  case Failure(e) => output.println("ERROR")
                }
                output.flush()
              } else {
                buffer.write(byte)
              }
              index += 1
            }

          } else {
            isConnected = false
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
      log.info("Socket has disconnected")
    }

  }


}
