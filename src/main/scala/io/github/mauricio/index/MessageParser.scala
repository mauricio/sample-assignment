package io.github.mauricio.index

import java.nio.charset.StandardCharsets

import io.netty.buffer.{ByteBuf, Unpooled}

import scala.util.Try

class NotEnoughPartsException(message: String)
  extends IllegalArgumentException(s"Every message must contain at least 2 parts - ${message}")

class OperationDoesNotExistException(operation: String)
  extends IllegalArgumentException(s"Operation [${operation}] does not exist")

class InvalidMessageException(message: String)
  extends IllegalArgumentException(message)

object MessageParser {

  val validSizes = Set(1, 2, 3)
  val allowedOperations = Map(
    "INDEX" -> IndexOperation,
    "REMOVE" -> RemoveOperation,
    "QUERY" -> QueryOperation)


  def parse(bytes: Array[Byte]): Try[Operation] =
    parse(Unpooled.wrappedBuffer(bytes))

  def parse(bytes: ByteBuf): Try[Operation] = {
    Try {
      val line = bytes.toString(StandardCharsets.UTF_8)
      bytes.readerIndex(bytes.readerIndex() + bytes.readableBytes())

      val pieces = line.split("[|]").map(_.trim)

      if (line.count(c => c == '|') != 2 || !validSizes.contains(pieces.length)) {
        throw new NotEnoughPartsException(line)
      }

      val (operation, packageName, dependencies) = pieces.length match {
        case 1 => (pieces(0), "", Set.empty[String])
        case 2 => (pieces(0), pieces(1), Set.empty[String])
        case _ => (pieces(0), pieces(1), pieces(2).split(",").map(_.trim).filterNot(_.isEmpty).toSet)
      }

      raiseIfNullOrEmpty(operation, "operation")
      raiseIfNullOrEmpty(packageName, "package")

      allowedOperations.get(operation) match {
        case Some(operationType) =>
          Operation(operationType, packageName, dependencies)
        case None =>
          throw new OperationDoesNotExistException(operation)
      }

    }
  }

  def raiseIfNullOrEmpty(value: String, name: String): Unit = {
    if (value == null || value.isEmpty) {
      throw new InvalidMessageException(s"Field ${name} can not be empty")
    }
  }

}
