package swalka

import java.nio.file.{Files, Path}
import java.time.LocalTime
import scala.concurrent.duration._
import swalka.writer.{FileWriter, SegmentedWriter}

object Util {
  def withTempDir[A](f: Path => A): A = {
    val tf = Files.createTempDirectory("test-spec")
    val r = f(tf)
    tf.toFile.delete()
    r
  }

  def withSegmentedLog[A](inputs: Iterable[String], segmentSize: Long)(f: Path => A): A = withTempDir { path =>
    val fw = new SegmentedWriter(path, segmentSize, 10.minutes)

    inputs.map(_.getBytes()).map(Record.wrap).map(Record.toByteBuffer).foreach(fw.write)
    fw.close

    f(path)
  }


  def withWrittenLog[A](f: Path => A): A = withTempDir { path =>
    val fw = new FileWriter(path, 0)
    val record = Record.wrap(s"āžņīļ Current local time is ${LocalTime.now().toString}".getBytes("UTF-8"))
    fw.write(Record.toByteBuffer(record))
    fw.flush
    fw.close
    f(path)
  }
}
