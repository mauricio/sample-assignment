package io.github.mauricio.index

import io.github.mauricio.index.server.MessageParser
import io.github.mauricio.index.util.Log

import scala.util.Try

object OperationExecutor {
  val log = Log.get[OperationExecutor]
}

class OperationExecutor( index : Index ) {

  def execute( bytes : Array[Byte] ) : Try[OperationResult] = {

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
