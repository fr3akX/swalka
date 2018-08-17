package com.ruskulis.scalawal

import java.nio.file.{Files, Path}
import java.time.LocalTime

import com.ruskulis.scalawal.writer.FileWriter

object Util {
  def withTempDir[A](f: Path => A): A = {
    val tf = Files.createTempDirectory("test-spec")
    tf.toFile.deleteOnExit()
    f(tf)
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
