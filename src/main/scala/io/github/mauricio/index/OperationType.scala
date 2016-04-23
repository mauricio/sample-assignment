package io.github.mauricio.index

sealed trait OperationType

object IndexOperation extends OperationType
object QueryOperation extends OperationType
object RemoveOperation extends OperationType
