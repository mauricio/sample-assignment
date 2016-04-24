package io.github.mauricio.index

import io.github.mauricio.index.util.Log
import io.netty.buffer.{ByteBuf, Unpooled}

import scala.util.Try

class OperationExecutor(index: Index) {

  def execute(bytes: Array[Byte]): Try[OperationResult] =
    execute(Unpooled.wrappedBuffer(bytes))

  def execute(bytes: ByteBuf): Try[OperationResult] = {

    MessageParser.parse(bytes).map {
      operation =>
        operation.operationType match {
          case IndexOperation => index.index(operation.packageName, operation.dependencies)
          case RemoveOperation => index.remove(operation.packageName)
          case QueryOperation => index.query(operation.packageName)
        }
    }

  }

}
