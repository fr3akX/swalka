package com.ruskulis.scalawal

import com.ruskulis.scalawal.offset.FileOffset
import org.scalatest.{FlatSpec, Matchers}

class OffsetSpec extends FlatSpec with Matchers {
  it should "be able to write offset and read it" in {
    val offset = new FileOffset

    offset.current shouldBe 0
    offset.commit(1000L)
    offset.close
    (new FileOffset).current shouldBe 1000L
  }
}
