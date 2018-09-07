package swalka

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import swalka.writer.FileWriter
import AWriterBenchmark._

import scala.io.Source

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class AWriterBenchmark {
  @Benchmark
  def walWriteWithFlush: Unit = {
    AWriterBenchmark.writer.write(Record.toByteBuffer(rec))
    AWriterBenchmark.writer.flush()
  }

  /*
   135.741 ops/ms
   */
  @Benchmark
  def walWriteWithoutFlush: Unit = {
    AWriterBenchmark.writer.write(Record.toByteBuffer(rec))
  }

}

object AWriterBenchmark {
  val rec = Record.wrap(Source.fromResource("small.json").mkString.getBytes())
  lazy val writer = new FileWriter(ReaderBenchmark.targetPath, 0)
}