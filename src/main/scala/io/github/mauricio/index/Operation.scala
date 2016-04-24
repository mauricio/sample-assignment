package io.github.mauricio.index

case class Operation(
                      operationType: OperationType,
                      packageName: String,
                      dependencies: Set[String] = Set.empty
                    )

