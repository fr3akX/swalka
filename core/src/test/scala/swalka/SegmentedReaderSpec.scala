package swalka

import org.scalatest.{FlatSpec, Matchers}
import swalka.Util.withTempDir
import swalka.offset.{FileOffset, Offset}
import swalka.reader.{CommitableReader, SegmentedReader}
import swalka.writer.SegmentedWriter
import scala.concurrent.duration._

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

  it should "not fail on empty state" in withTempDir { dir =>
    val sw = new SegmentedWriter(dir, 1, 60.seconds)
    sw.close()

    val offset = new FileOffset("test", dir)
    val segReader = new SegmentedReader(dir, offset.current)
    val reader = new CommitableReader(segReader, offset)
    reader.hasNext
    succeed
  }
}
