package com.evojam.macros

import org.specs2.mutable.Specification

class IdMacroSpec extends Specification {

  @id case class FooId(value: String)

  "@id" should {

    "generate require" in {
      FooId(null) must throwA[IllegalArgumentException]
    }

    "generate unapply" in {
      FooId.unapply("5") must beSome(FooId("5"))
    }
  }
}
