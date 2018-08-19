package swalka

import java.nio.file.Paths
import java.util.concurrent.Executors

import swalka.offset.FileOffset
import swalka.offset.Offset.Current
import swalka.reader.SegmentedReader
import swalka.watch.Watcher

object ReaderApp extends App {

  val dbPath = Paths.get("./benchmark/target")
  val currentExec = Executors.newSingleThreadExecutor()

  val watcher = new Watcher(dbPath)

  watcher.registerEventHandler { p =>
    println(s"Event on: ${p.toString}")
    currentExec.execute(() => doRead())
  }

  def doRead(): Unit = {
    println("Doing read")

    val offset = new FileOffset(dbPath)
    println(s"current offset: ${offset.current}")
    val reader = new SegmentedReader(dbPath, offset.current)

    var current: Current = Current(0, 0)
    while (reader.hasNext) {
      val nextRec = reader.next
//      println(s"Commiting coffset ${nextRec.offset}")
      offset.commit(nextRec.offset)
      current = nextRec.offset
    }
    offset.close
    reader.close
    println(s"Done reading, last offset: $current")
  }

  currentExec.execute(() => doRead())
}
