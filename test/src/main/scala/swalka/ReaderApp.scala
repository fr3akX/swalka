package swalka

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.StrictLogging
import swalka.streams.WatchedReaderSourceStage
import swalka.watch.FSWatcher

object ReaderApp extends App with StrictLogging {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  import system.dispatcher

  sys.addShutdownHook {
    mat.shutdown()
    system.terminate()
  }

  val dbPath = Paths.get("./benchmark/target")
  val watcher = new FSWatcher(dbPath)
  val readerSource = Source.fromGraph(new WatchedReaderSourceStage("app", dbPath, watcher.registerEventHandler))
  val end = readerSource.runForeach { s =>
    logger.debug(new String(s.data))
    s.commit()
  }

  end.onComplete { _ =>
    system.terminate()
  }
}
