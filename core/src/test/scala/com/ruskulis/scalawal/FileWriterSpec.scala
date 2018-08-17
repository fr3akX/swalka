package com.ruskulis.scalawal

import java.nio.file.Paths
import java.time.LocalTime

import com.ruskulis.scalawal.writer.FileWriter
import org.scalatest.{FlatSpec, Matchers}

class FileWriterSpec extends FlatSpec with Matchers {
  it should "create log file" in {
    val fw = new FileWriter(Paths.get("journal.log"), 1)
    val record = Record.wrap(s"āžņīļ Current local time is ${LocalTime.now().toString}".getBytes("UTF-8"))
    fw.write(Record.toByteBuffer(record))
    fw.flush
    fw.close
    succeed
  }
}
