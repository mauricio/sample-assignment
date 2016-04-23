package io.github.mauricio.index

import scala.util.{Failure, Success, Try}

sealed trait OperationResult

object Ok extends OperationResult
object Fail extends OperationResult
