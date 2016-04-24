package io.github.mauricio.index

sealed trait OperationResult

object Ok extends OperationResult

object Fail extends OperationResult
