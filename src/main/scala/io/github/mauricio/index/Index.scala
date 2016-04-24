package io.github.mauricio.index

import java.util.concurrent.locks.{Lock, ReentrantReadWriteLock}

class Index {

  private val readWriteLock = new ReentrantReadWriteLock()
  private val packages = scala.collection.mutable.Map[String, Package]()

  def index(name: String, dependencies: Set[String] = Set.empty): OperationResult =
    withWriteLock {
      if (!dependencies.subsetOf(packages.keySet)) {
        Fail
      } else {

        val currentPackage = new Package(name, dependencies)

        packages.get(name) match {
          case Some(existingPackage) => {
            val (dependenciesToRegister, dependenciesToDeregister) = existingPackage.diffDependencies(dependencies)
            currentPackage.addDependents(existingPackage.allDependents)
            registerDependencies(name, dependenciesToRegister)
            deregisterDependencies(name, dependenciesToDeregister)
          }
          case None => {
            registerDependencies(name, dependencies)
          }
        }

        packages.update(name, currentPackage)

        Ok
      }
    }

  def query(packageName: String): OperationResult =
    withReadLock {
      packages.get(packageName) match {
        case Some(name) => Ok
        case None => Fail
      }
    }

  private def withReadLock[A](f: => A): A =
    withLock(readWriteLock.readLock(), f)

  def remove(name: String): OperationResult =
    withWriteLock {
      packages.get(name) match {
        case Some(currentPackage) => {
          if (currentPackage.hasDependents()) {
            Fail
          } else {
            packages.remove(name)
            deregisterDependencies(name, currentPackage.dependencies)
            Ok
          }
        }
        case None => Ok
      }
    }

  private def deregisterDependencies(name: String, dependencies: Set[String]): Unit =
    foreachDependency(dependencies, _.removeDependent(name))

  private def foreachDependency[A](dependencies: Set[String], f: Package => A): Unit = {
    withWriteLock {
      dependencies.foreach {
        dependency =>
          packages.get(dependency).foreach(f)
      }
    }
  }

  private def withWriteLock[A](f: => A): A =
    withLock(readWriteLock.writeLock(), f)

  private def withLock[A](lock: Lock, f: => A): A = {
    lock.lock()
    try {
      f
    } finally {
      lock.unlock()
    }
  }

  private def registerDependencies(name: String, dependencies: Set[String]): Unit =
    foreachDependency(dependencies, _.addDependent(name))

}
