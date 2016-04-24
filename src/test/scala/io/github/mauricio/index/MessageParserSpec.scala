package io.github.mauricio.index

import java.nio.charset.StandardCharsets

import org.specs2.mutable.Specification

import scala.util.{Success, Try}

class MessageParserSpec extends Specification {

  def parse(text: String): Try[Operation] =
    MessageParser.parse(text.getBytes(StandardCharsets.UTF_8))

  "parser" >> {

    "it parses a good message" >> {
      parse("INDEX|cloog|gmp,isl,pkg-config") ===
        Success(Operation(IndexOperation, "cloog", Set("gmp", "isl", "pkg-config")))
    }

    "it parses a message that does not have any dependencies" >> {
      parse("INDEX|cloog|") ===
        Success(Operation(IndexOperation, "cloog"))
    }

    "it parses a query message" >> {
      parse("QUERY|pkg|") === Success(Operation(QueryOperation, "pkg"))
    }

    "it parses a remove message" >> {
      parse("REMOVE|pkg|") === Success(Operation(RemoveOperation, "pkg"))
    }

    "it fails to parse a message that is missing a part" >> {
      parse("QUERY|cloog").failed.get must beAnInstanceOf[NotEnoughPartsException]
    }

    "it fails to parse if the operation is unknown" >> {
      parse("WHAT|cloog|").failed.get must beAnInstanceOf[OperationDoesNotExistException]
    }

    "it fails to parse if the package name is empty" >> {
      parse("REMOVE||").failed.get must beAnInstanceOf[InvalidMessageException]
    }

  }

}
