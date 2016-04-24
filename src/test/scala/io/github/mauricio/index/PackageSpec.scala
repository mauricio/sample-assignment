package io.github.mauricio.index

import org.specs2.mutable.Specification

class PackageSpec extends Specification {

  "package" >> {

    "it adds a dependency to the package" >> {
      val p = new Package("scala", Set("java"))
      p.addDependent("scalaz")

      p.allDependents === Set("scalaz")
    }

    "it adds many dependencies to a package" >> {
      val p = new Package("scala", Set("java"))
      p.addDependents(Set("scalaz", "specs2"))

      p.allDependents === Set("scalaz", "specs2")
    }

    "it removes a dependency from a package" >> {
      val p = new Package("scala", Set("java"))
      p.addDependents(Set("scalaz", "specs2"))
      p.removeDependent("specs2")

      p.allDependents === Set("scalaz")
    }

    "it produces a diff between the packages" >> {
      val p = new Package("scala", Set("java", "sbt"))
      p.diffDependencies(Set("java", "icu")) === (Set("icu") -> Set("sbt"))
    }

    "it produces an empty diff if there are no differences" >> {
      val p = new Package("scala", Set("java", "sbt"))
      p.diffDependencies(Set("java", "sbt")) === (Set.empty[String] -> Set.empty[String])
    }

  }

}
