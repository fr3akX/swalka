package com.ruskulis.scalawal

import com.ruskulis.scalawal.reader.SegmentedReader
import org.scalatest.{FlatSpec, Matchers}

class SegmentedReaderSpec extends FlatSpec with Matchers {

  val seedData = List("abc", "cde", "adadadasdada", "adadsada")

  //segment size 1, forces every item in new segment
  it should "be able to read segmented log" in Util.withSegmentedLog(seedData, 1) { dir =>
    val rdr = new SegmentedReader(dir)
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
