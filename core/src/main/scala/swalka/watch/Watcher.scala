package swalka.watch

import java.nio.file.{FileSystems, Path}
import java.nio.file.StandardWatchEventKinds._
import java.util.concurrent.{Executors, TimeUnit}
import scala.collection.JavaConverters._

class Watcher(path: Path) {

  @volatile
  private var isCanceled = false

  private val ec = Executors.newSingleThreadExecutor()
  private val ws = FileSystems.getDefault.newWatchService()
  path.register(ws, ENTRY_CREATE, ENTRY_MODIFY)

  def registerEventHandler[A](f: Path => A): Unit = {
    ec.execute { () =>
      while(!isCanceled) {
        val wk = ws.poll(5, TimeUnit.SECONDS)
        if(wk != null) wk.pollEvents().asScala.foreach { we => f(we.context().asInstanceOf[Path]) }
      }
    }
  }

  def close(): Unit = {
    ws.close()
    isCanceled = true
    ec.shutdown()
  }
}
