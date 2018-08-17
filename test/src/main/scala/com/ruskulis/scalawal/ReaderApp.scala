package com.ruskulis.scalawal

import java.nio.file.{FileSystems, Path, Paths, StandardWatchEventKinds}

import com.ruskulis.scalawal.offset.FileOffset
import com.ruskulis.scalawal.reader.FileReader

import scala.collection.JavaConverters._
object ReaderApp extends App {
  val offset = new FileOffset

  println(s"current offset: ${offset.current}")
  val reader = new FileReader(offset.current)
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
