package swalka

import org.scalatest.{FlatSpec, Matchers}
import swalka.offset.FileOffset
import swalka.offset.Offset.Current

class OffsetSpec extends FlatSpec with Matchers {
  it should "be able to write offset and read it" in Util.withTempDir { path =>

    val offset = new FileOffset("test", path)

    offset.current shouldBe Current(0, 0)
    offset.commit(Current(99, 1000L))
    offset.close
    new FileOffset("test", path).current shouldBe Current(99, 1000L)
  }
}
