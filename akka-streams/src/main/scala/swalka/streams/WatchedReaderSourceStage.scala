package swalka.streams

import java.nio.file.Path

import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import swalka.offset.FileOffset
import swalka.reader.CommitableReader.CommitableResult
import swalka.reader.{CommitableReader, SegmentedReader}
import swalka.watch.Watcher

class WatchedReaderSourceStage(readerId: String, dbPath: Path) extends GraphStage[SourceShape[CommitableResult]] {
  val out: Outlet[CommitableResult] = Outlet("CommitableResultSource")

  override def shape: SourceShape[CommitableResult] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    var offset = new FileOffset(readerId, dbPath)
    var segReader = new SegmentedReader(dbPath, offset.current)
    var reader = new CommitableReader(segReader, offset)

    var downStreamDemands: Boolean = false
    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        if(reader.hasNext) push(out, reader.next)
        else downStreamDemands = true
      }
    })

    def reloadOnEof() = {
      if(!reader.hasNext) {
        offset.close
        segReader.close
        reader.close

        offset = new FileOffset(readerId, dbPath)
        segReader = new SegmentedReader(dbPath, offset.current)
        reader = new CommitableReader(segReader, offset)
      }
      if(reader.hasNext && downStreamDemands && isAvailable(out)) {
        downStreamDemands = false
        push(out, reader.next)
      }
    }

    val watcher = new Watcher(dbPath)
    val pollCB: AsyncCallback[Unit] = getAsyncCallback[Unit] { _ => reloadOnEof() }

    watcher.registerEventHandler { p =>
      println(s"Event on: ${p.toString}")
      pollCB.invoke()
    }

    override def postStop(): Unit = {
      watcher.close()
      offset.close
      reader.close
      segReader.close
      super.postStop()
    }
  }
}
