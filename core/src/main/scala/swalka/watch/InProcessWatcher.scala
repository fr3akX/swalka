package swalka.watch

import java.nio.file.Path

import swalka.watch.InProcessWatcher.OnPublish

class InProcessWatcher {

  var handler: Path => Unit = _

  def registerEventHandler(f: Path => Unit): Unit = {
    handler = f
  }

  def createWriterCallback: OnPublish = path => {
    if(handler != null) handler.apply(path)
  }
}

object InProcessWatcher {
  type OnPublish = Path => Unit
}