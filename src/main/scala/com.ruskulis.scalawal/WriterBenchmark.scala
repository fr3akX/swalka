package com.ruskulis.scalawal

import java.util.concurrent.TimeUnit

import com.ruskulis.scalawal.writer.FileWriter
import org.openjdk.jmh.annotations._

import scala.io.Source

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class WriterBenchmark {
  @Benchmark
  def walWrite: Unit = {
    WriterBenchmark.writer.write(WriterBenchmark.randData)
    //WriterBenchmark.writer.flush
  }

}

object WriterBenchmark {
  val randData = Source.fromResource("small.json").mkString.getBytes()
  val writer = new FileWriter
}