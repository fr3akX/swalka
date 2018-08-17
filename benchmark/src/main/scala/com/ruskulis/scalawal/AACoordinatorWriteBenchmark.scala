package com.ruskulis.scalawal

import java.util.concurrent.TimeUnit

import com.ruskulis.scalawal.AWriterBenchmark._
import com.ruskulis.scalawal.writer.{FileWriter, WriterCoordinator}
import org.openjdk.jmh.annotations._

import scala.io.Source

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class AACoordinatorWriteBenchmark {
  @Benchmark
  def walWriteWithoutFlush: Unit = {
    AACoordinatorWriteBenchmark.writer.write(Record.toByteBuffer(rec))
  }
}

object AACoordinatorWriteBenchmark {
  val rec = Record.wrap(Source.fromResource("small.json").mkString.getBytes())
  lazy val writer = new WriterCoordinator(ReaderBenchmark.targetPath, 1024 * 1024 * 1024 * 1)
}
