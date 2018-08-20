package swalka

import java.time.LocalTime

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{FlatSpec, Matchers}
import swalka.writer.SegmentedWriter

class SegmentedWriterSpec extends FlatSpec with Matchers with StrictLogging {
  it should "be able to write trough coordinator" in Util.withTempDir { path =>
    val fw = new SegmentedWriter(path, 1024)
    val record = Record.wrap(s"āžņīļ Current local time is ${LocalTime.now().toString}".getBytes("UTF-8"))
    fw.write(Record.toByteBuffer(record))
    fw.flush
    fw.close

    logger.debug(s"on ${path.toString}")
    succeed
  }
}
