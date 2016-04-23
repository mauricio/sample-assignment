package io.github.mauricio.index

class Package(val name: String, val dependencies: Set[String]) {

  private val dependents = scala.collection.mutable.Set[String]()

  def addDependents(names: String*) : Unit =
    dependents ++= names

  def removeDependents(names : String*) : Unit =
    dependents --= names

  def allDependents : scala.collection.Set[String] = dependents

  def hasDependents() : Boolean = dependents.nonEmpty

  def diffDependencies( newDependencies : Set[String] ) : (Set[String],Set[String]) = {
    newDependencies.diff(dependencies) ->
      dependencies.diff(newDependencies)
  }

}
