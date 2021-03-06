package swalka

import java.util.concurrent.TimeUnit

import AWriterBenchmark._
import org.openjdk.jmh.annotations._
import swalka.writer.SegmentedWriter

import scala.io.Source
import scala.concurrent.duration._

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class ACoordinatorWriterBenchmark {
  @Benchmark
  def walWriteWithFlush: Unit = {
    ACoordinatorWriterBenchmark.writer.write(Record.toByteBuffer(rec))
    ACoordinatorWriterBenchmark.writer.flush()
  }

  @Benchmark
  def walWriteWithoutFlush: Unit = {
    ACoordinatorWriterBenchmark.writer.write(Record.toByteBuffer(rec))
  }
}

object ACoordinatorWriterBenchmark {
  val rec = Record.wrap(Source.fromResource("small.json").mkString.getBytes())
  lazy val writer = new SegmentedWriter(ReaderBenchmark.targetPath, 1024 * 1024 * 1024 * 1, 60.seconds)
}
