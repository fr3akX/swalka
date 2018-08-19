package swalka
import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import swalka.reader.FileReader
import ReaderBenchmark._

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
  val targetPath: Path = Paths.get("").resolve("target")
  lazy val walReader = new FileReader(targetPath, 0, 0)
}
