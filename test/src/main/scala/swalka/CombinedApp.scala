package swalka

import java.nio.file.Paths
import java.time.Instant
import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import swalka.streams.{PersistentWriterFlow, WatchedReaderFlow}
import swalka.writer.{HouseKeeper, SegmentedWriter}

import scala.concurrent.duration._

object CombinedApp extends App with StrictLogging {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  sys.addShutdownHook {
    mat.shutdown()
    system.terminate()
  }
  import system.dispatcher

  val dbPath = Paths.get("./benchmark/target")
  val exec = Executors.newSingleThreadScheduledExecutor()
  val houseKeeper = new HouseKeeper(dbPath, exec, 1.minute)
  val cancellation = houseKeeper.start()

  val persistentWriterFlow = new PersistentWriterFlow(new SegmentedWriter(dbPath, 1024 * 1, 10.minutes))
  val readerFlow = new WatchedReaderFlow("app", dbPath)

  val run = Source.tick(1.second, 1.second, s"Hello world ${ Instant.now().toString }")
    .map(s => Record.wrap(s.getBytes()))
    .via(persistentWriterFlow)
    .via(readerFlow).map { m =>
    println("got element")
    m
  }.map(_.commit()).runWith(Sink.ignore)


  run.onComplete { x =>
    println("Comlpleted: " + x)
    cancellation.close()
  }

}
