package swalka

import java.time.LocalTime

import org.scalatest.{FlatSpec, Matchers}
import swalka.writer.FileWriter

class FileWriterSpec extends FlatSpec with Matchers {
  it should "create log file" in Util.withTempDir { path =>
    val fw = new FileWriter(path, 1)
    val record = Record.wrap(s"āžņīļ Current local time is ${LocalTime.now().toString}".getBytes("UTF-8"))
    fw.write(Record.toByteBuffer(record))
    fw.flush()
    fw.close()
    succeed
  }
}
