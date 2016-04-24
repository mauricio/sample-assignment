package io.github.mauricio.index.server

import java.io.BufferedInputStream
import java.net.Socket
import java.nio.charset.StandardCharsets

import io.github.mauricio.index.util.Constants

import scala.collection.mutable.ArrayBuffer

class ClientSocket(val port: Int) {

  private val socket = new Socket(Constants.DefaultHost, port)
  socket.setSoTimeout(Constants.ReadTimeout)
  private val output = socket.getOutputStream
  private val input = new BufferedInputStream(socket.getInputStream)

  def index(name: String, dependencies: Set[String] = Set.empty): String =
    write(s"INDEX|${name}|${dependencies.mkString(",")}")

  def write(message: String): String = {
    output.write(message.getBytes(StandardCharsets.UTF_8))
    output.write(Constants.NewLine)
    output.flush()
    read()
  }

  private def read(): String = {
    val buffer = new ArrayBuffer[Byte](128)

    var continue = true

    while (continue) {
      val byte = input.read().toByte
      if (byte == Constants.NewLine) {
        continue = false
      } else {
        buffer.append(byte)
      }
    }

    new String(buffer.toArray, StandardCharsets.UTF_8)
  }

  def query(name: String): String =
    write(s"QUERY|${name}|")

  def remove(name: String): String =
    write(s"REMOVE|${name}|")

  def close(): Unit = {
    socket.close()
  }

}
