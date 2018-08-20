package swalka.watch

import java.nio.file.{FileSystems, Path}
import java.nio.file.StandardWatchEventKinds._
import java.util.concurrent.{Executors, TimeUnit}

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

class FSWatcher(path: Path) {

  @volatile
  private var isCanceled = false

  private val ec = Executors.newSingleThreadExecutor()
  private val ws = FileSystems.getDefault.newWatchService()

  private val myWk = path.register(ws, ENTRY_CREATE, ENTRY_MODIFY)

  def registerEventHandler[A](f: Path => A): Unit = {
    ec.execute { () =>
      while(!isCanceled) {
        val wk = ws.poll(5, TimeUnit.SECONDS)
        if(wk != null) {
          wk.pollEvents().asScala.foreach { we =>
            try {
              val p = we.context().asInstanceOf[Path]
              f(p)
            } catch {
              case NonFatal(e) =>
                e.printStackTrace()
            }
          }
          wk.reset()
        }
      }
    }
  }

  def close(): Unit = {
    isCanceled = true
    ec.shutdown()
    ws.close()
  }
}
