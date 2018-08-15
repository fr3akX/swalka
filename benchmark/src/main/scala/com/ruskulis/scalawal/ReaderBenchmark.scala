package com.ruskulis.scalawal
import java.util.concurrent.TimeUnit

import com.ruskulis.scalawal.ReaderBenchmark._
import com.ruskulis.scalawal.reader.FileReader
import org.openjdk.jmh.annotations._

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class ReaderBenchmark {

  /*
  ByteBufers ReaderBenchmark.walRead  thrpt    3  282.297 ± 243.796  ops/ms
   InputStream read ReaderBenchmark.walRead  thrpt    3  194.861 ± 41.290  ops/ms
   */
  @Benchmark
  def walRead: Unit = {
    walReader.next
  }
}

object ReaderBenchmark {
  val walReader = new FileReader(0)
}
