package swalka

import java.nio.file.Paths
import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import swalka.streams.WriterSinkStage
import swalka.writer.SegmentedWriter

import scala.concurrent.duration._

object WriterApp extends App {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  sys.addShutdownHook {
    mat.shutdown()
    system.terminate()
  }
  val dbPath = Paths.get("./benchmark/target")

  val writerSink = Sink.fromGraph(new WriterSinkStage(new SegmentedWriter(dbPath, 1024 * 1024 * 1)))
  Source.tick(1.second, 1.second, s"Hello world ${Instant.now().toString}")
    .map(s => Record.wrap(s.getBytes()))
    .to(writerSink).run()
}
