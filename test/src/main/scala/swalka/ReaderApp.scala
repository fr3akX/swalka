package swalka

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import swalka.streams.WatchedReaderSourceStage

object ReaderApp extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  import system.dispatcher

  sys.addShutdownHook {
    mat.shutdown()
    system.terminate()
  }

  val dbPath = Paths.get("./benchmark/target")
  val readerSource = Source.fromGraph(new WatchedReaderSourceStage("app", dbPath))
  val end = readerSource.runForeach { s =>
    println(new String(s.data))
    s.commit()
  }

  end.onComplete { _ =>
    system.terminate()
  }
}
