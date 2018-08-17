package com.ruskulis.scalawal

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import com.ruskulis.scalawal.WriterBenchmark._
import com.ruskulis.scalawal.writer.FileWriter
import org.openjdk.jmh.annotations._

import scala.io.Source

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class WriterBenchmark {
  @Benchmark
  def walWriteWithFlush: Unit = {
    WriterBenchmark.writer.write(Record.toByteBuffer(rec))
    WriterBenchmark.writer.flush
  }

  @Benchmark
  def walWriteWithoutFlush: Unit = {
    WriterBenchmark.writer.write(Record.toByteBuffer(rec))
  }

}

object WriterBenchmark {
  val rec = Record.wrap(Source.fromResource("small.json").mkString.getBytes())
  val writer = new FileWriter(Paths.get("."), 0)
}