package io.github.mauricio.index

sealed trait Operation

object IndexOperation extends Operation
object QueryOperation extends Operation
object RemoveOperation extends Operation
