package com.ruskulis.scalawal

import java.util.concurrent.TimeUnit

import com.ruskulis.scalawal.AWriterBenchmark._
import com.ruskulis.scalawal.writer.{FileWriter, SegmentedWriter}
import org.openjdk.jmh.annotations._

import scala.io.Source

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class ACoordinatorWriterBenchmark {
  @Benchmark
  def walWriteWithFlush: Unit = {
    ACoordinatorWriterBenchmark.writer.write(Record.toByteBuffer(rec))
    ACoordinatorWriterBenchmark.writer.flush
  }

  @Benchmark
  def walWriteWithoutFlush: Unit = {
    ACoordinatorWriterBenchmark.writer.write(Record.toByteBuffer(rec))
  }
}

object ACoordinatorWriterBenchmark {
  val rec = Record.wrap(Source.fromResource("small.json").mkString.getBytes())
  lazy val writer = new SegmentedWriter(ReaderBenchmark.targetPath, 1024 * 1024 * 1024 * 1)
}
