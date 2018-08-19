package swalka

import java.nio.file.Paths
import java.util.concurrent.{Executors, LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import swalka.offset.FileOffset
import swalka.offset.Offset.Current
import swalka.reader.{CommitableReader, SegmentedReader}
import swalka.watch.Watcher

import scala.util.Try

object ReaderApp extends App {

  val dbPath = Paths.get("./benchmark/target")
//  val currentExec = Executors.newSingleThreadExecutor()
  val currentExec = new ThreadPoolExecutor(1, 1,
    0L, TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue[Runnable](1))


  val watcher = new Watcher(dbPath)

  watcher.registerEventHandler { p =>
    println(s"Event on: ${p.toString}")
    Try(currentExec.execute(() => doRead()))
  }

  def doRead(): Unit = {
    println("Doing read")

    val offset = new FileOffset(dbPath)
    val segReader = new SegmentedReader(dbPath, offset.current)

    println(s"current offset: ${offset.current}")
    val reader = new CommitableReader(segReader, offset)

    var current: Current = Current(0, 0)
    while (reader.hasNext) {
      val nextRec = reader.next
      nextRec.commit()
      current = nextRec.offset
    }
    offset.close
    segReader.close
    println(s"Done reading, last offset: $current")
  }
  currentExec.execute(() => doRead())
}
