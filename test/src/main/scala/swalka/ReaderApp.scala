package swalka

import java.nio.file.{FileSystems, Path, Paths, StandardWatchEventKinds}

import swalka.offset.FileOffset
import swalka.reader.FileReader
import scala.collection.JavaConverters._

object ReaderApp extends App {
  val offset = new FileOffset(Paths.get("."))

  println(s"current offset: ${offset.current}")
  val reader = new FileReader(Paths.get("."), 0, offset.current)
  val ws = FileSystems.getDefault.newWatchService()


  def waitModification(): Unit = {
    val path = Paths.get(".")
    val watchKey = path.register(ws, StandardWatchEventKinds.ENTRY_MODIFY)
    println("Waiting for events")
    val wk = ws.take()
    if(wk.pollEvents().asScala.map(_.context().asInstanceOf[Path]).exists(_.endsWith("journal.log"))){
      wk.reset()
    } else {
      waitModification()
    }
  }

  def doRead(): Unit = {
    if(reader.hasNext) {
      val n = reader.next
      println(s"Commiting offset: ${n.offset}")
      offset.commit(n.offset)
      doRead()
    } else {
      println("busy looping")
      waitModification()
      doRead()
    }
  }


  doRead()
}
