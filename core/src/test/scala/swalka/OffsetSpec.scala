package swalka

import org.scalatest.{FlatSpec, Matchers}
import swalka.offset.FileOffset

class OffsetSpec extends FlatSpec with Matchers {
  it should "be able to write offset and read it" in Util.withTempDir { path =>

    val offset = new FileOffset(path)

    offset.current shouldBe 0
    offset.commit(1000L)
    offset.close
    new FileOffset(path).current shouldBe 1000L
  }
}
