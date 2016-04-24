package io.github.mauricio.index

class Package(val name: String, val dependencies: Set[String]) {

  private val dependents = scala.collection.mutable.Set[String]()

  def addDependent(name: String): Unit =
    dependents += name

  def addDependents(names: scala.collection.Set[String]): Unit =
    dependents ++= names

  def removeDependent(name: String): Unit =
    dependents -= name

  def allDependents: scala.collection.Set[String] = dependents

  def hasDependents(): Boolean = dependents.nonEmpty

  def diffDependencies(newDependencies: Set[String]): (Set[String], Set[String]) = {
    newDependencies.diff(dependencies) ->
      dependencies.diff(newDependencies)
  }

}
