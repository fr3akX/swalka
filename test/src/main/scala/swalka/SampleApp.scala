package swalka

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import swalka.reader.CommitableReader.CommitableResult
import swalka.streams.{WatchedReaderFlow, WriterNotifier}
import swalka.writer.{HouseKeeper, SegmentedWriter}

import scala.concurrent.duration._

object SampleApp extends App with StrictLogging {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  sys.addShutdownHook {
    mat.shutdown()
    system.terminate()
  }

  val processorSink = Sink.foreach[CommitableResult] { c =>
    println("Processing item: " + new String(c.data))
    c.commit()
  }

  val swalkaSink = {
    val dbPath = Paths.get("./target")
    SegmentedWriter.init(dbPath)
    val hk = HouseKeeper(dbPath).start()

    val readerFlow = WatchedReaderFlow("sample-app", dbPath)
    val queue = WatchedReaderFlow.notifiableProcessor(readerFlow, processorSink).run()

    val writer = WriterNotifier(queue, new SegmentedWriter(dbPath, 1024 * 1024 * 1024, 10.minutes))

    system.registerOnTermination {
      hk.close()
      writer.close()
      queue.complete()
    }

    s: String => writer.write(Record.make(s))
  }

  Source.tick(1.second, 1.second, "Hello world").runForeach(swalkaSink)
}
