package com.ruskulis.scalawal

import java.time.LocalTime

import com.ruskulis.scalawal.writer.SegmentedWriter
import org.scalatest.{FlatSpec, Matchers}

class SegmentedWriterSpec extends FlatSpec with Matchers {
  it should "be able to write trough coordinator" in Util.withTempDir { path =>
    val fw = new SegmentedWriter(path, 1024)
    val record = Record.wrap(s"āžņīļ Current local time is ${LocalTime.now().toString}".getBytes("UTF-8"))
    fw.write(Record.toByteBuffer(record))
    fw.flush
    fw.close

    println(path)
    succeed
  }
}
