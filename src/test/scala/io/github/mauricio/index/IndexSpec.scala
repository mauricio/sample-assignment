package io.github.mauricio.index

import org.specs2.mutable.Specification

class IndexSpec extends Specification {

  "index" >> {

    "indexes a package without dependencies" >> {
      val index = new Index()

      index.index("redis") === Ok
      index.query("redis") === Ok
    }

    "fails if a package has missing dependencies" >> {
      val index = new Index()
      index.index("scala", Set("java")) === Fail
    }

    "fails if a package does not exist" >> {
      val index = new Index()
      index.query("does-not-exist") === Fail
    }

    "ok's if trying to remove a package that does not exist" >> {
      val index = new Index()
      index.remove("not-here") === Ok
    }

    "ok's if trying to remove a package that exists and has no dependencies" >> {
      val index = new Index()
      index.index("java") === Ok
      index.remove("java") === Ok
    }

    "fails to remove package that is still dependend upon" >> {
      val index = new Index()
      index.index("scala") === Ok
      index.index("scalaz", Set("scala")) === Ok
      index.remove("scala") === Fail
    }

    "fail to remove if package has overwritten dependents" >> {
      val index = new Index()
      index.index("scala") === Ok
      index.index("scalaz") === Ok
      index.index("specs2", Set("scala")) === Ok
      index.index("specs2", Set("scala", "scalaz")) === Ok

      index.remove("scalaz") === Fail
      index.remove("specs2") === Ok
      index.remove("scalaz") === Ok
    }

    "removes package if dependent has removed dependency when overwritten" >> {
      val index = new Index()
      index.index("scala") === Ok
      index.index("scalaz") === Ok
      index.index("specs2", Set("scala", "scalaz")) === Ok
      index.index("specs2", Set("scala")) === Ok

      index.remove("scalaz") === Ok
      index.remove("specs2") === Ok
    }

  }


}
