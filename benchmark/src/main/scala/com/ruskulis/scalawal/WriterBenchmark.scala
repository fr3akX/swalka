package com.ruskulis.scalawal

import java.util.concurrent.TimeUnit

import com.ruskulis.scalawal.WriterBenchmark._
import com.ruskulis.scalawal.writer.FileWriter
import org.openjdk.jmh.annotations._

import scala.io.Source


/**
  *  WO flush
  *  WriterBenchmark.walWrite  thrpt    3  165.124 ± 28.221  ops/ms
  *  WriterBenchmark.walWrite  thrpt    3  171275.781 ± 30545.830  ops/s
  *  With Flush
  *  WriterBenchmark.walWrite  thrpt    3  12.915 ± 3.000  ops/ms
  *  WriterBenchmark.walWrite  thrpt    3  12627.732 ± 989.477  ops/s
  */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class WriterBenchmark {
  @Benchmark
  def walWrite: Unit = {
    //[info] WriterBenchmark.walWrite  thrpt    3  7109.277 ± 2347.298  ops/ms
    // wihout arrays [info] WriterBenchmark.walWrite  thrpt    3  24714.269 ± 6540.441  ops/ms
    WriterBenchmark.writer.write(Record.toData(rec))
    WriterBenchmark.writer.flush
  }

}

object WriterBenchmark {
  val rec = Record.fromWriter(Source.fromResource("small.json").mkString.getBytes())
  val writer = new FileWriter
}