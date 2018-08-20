package swalka

import java.nio.file.Paths
import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Broadcast, Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import swalka.streams.{WatchedReaderSourceStage, WriterSinkStage}
import swalka.watch.InProcessWatcher
import swalka.writer.SegmentedWriter

import scala.concurrent.duration._

object CombinedApp extends App with StrictLogging {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  import system.dispatcher

  sys.addShutdownHook {
    mat.shutdown()
    system.terminate()
  }

  val watcher = new InProcessWatcher
  val writerCb = watcher.createWriterCallback

  val dbPath = Paths.get("./benchmark/target")

  val writerSink = Sink.fromGraph(new WriterSinkStage(new SegmentedWriter(dbPath, 1024 * 1024 * 1)))
  Source.tick(1.second, 1.second, s"Hello world ${Instant.now().toString}")
    .map(s => Record.wrap(s.getBytes()))
    .to(Sink.combine(writerSink, Sink.foreach[Record](_ => writerCb.apply(Paths.get("."))))(Broadcast[Record](_))).run()

  val readerSource = Source.fromGraph(new WatchedReaderSourceStage("app", dbPath, watcher.registerEventHandler))
  val end = readerSource.runForeach { s =>
    logger.debug("From reader: " + new String(s.data))
    s.commit()
  }

  end.onComplete { _ =>
    system.terminate()
  }

}
