package swalka

import org.scalatest.{FlatSpec, Matchers}
import swalka.reader.SegmentedReader

class SegmentedReaderSpec extends FlatSpec with Matchers {

  val seedData = List("abc", "cde", "adadadasdada", "adadsada")

  //segment size 1, forces every item in new segment
  it should "be able to read segmented log" in Util.withSegmentedLog(seedData, 1) { dir =>
    val rdr = new SegmentedReader(dir, SegmentedReader.Beginning)
    var result = List.empty[String]
    def doRead(): Unit = {
      if(rdr.hasNext) {
        result = new String(rdr.next.data) :: result
        doRead()
      }
    }

    doRead()

    seedData shouldBe result.reverse
  }
}
