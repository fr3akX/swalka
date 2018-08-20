package swalka.streams

import java.nio.file.Path

import akka.stream.stage._
import akka.stream.{Attributes, Outlet, SourceShape}
import swalka.offset.FileOffset
import swalka.reader.CommitableReader.CommitableResult
import swalka.reader.{CommitableReader, SegmentedReader}


/**
  * TODO logic: do not close file on eof until it has been marked as closed in segments
  * @param readerId
  * @param dbPath
  */
class WatchedReaderSourceStage(readerId: String, dbPath: Path, registerListener: (Path => Unit) => Unit) extends GraphStage[SourceShape[CommitableResult]] {
  val out: Outlet[CommitableResult] = Outlet("CommitableResultSource")

  override def shape: SourceShape[CommitableResult] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with StageLogging {

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
      if(!reader.hasNext) { // && reader.isEof
        log.debug("Log is has no next, probably eof, trying reopen segments")
        offset.close
        segReader.close
        reader.close

        offset = new FileOffset(readerId, dbPath)
        segReader = new SegmentedReader(dbPath, offset.current)
        reader = new CommitableReader(segReader, offset)

        log.debug(s"Reading log from ${offset.current}")
      }
      if(reader.hasNext && downStreamDemands && isAvailable(out)) {
        log.debug("Emiting output")
        downStreamDemands = false
        push(out, reader.next)
      }
    }

    val pollCB: AsyncCallback[Unit] = getAsyncCallback[Unit] { _ => reloadOnEof() }


    override def preStart(): Unit = {
      super.preStart()
      log.debug(s"Initialized, reading from: ${offset.current}")
      log.debug("Registering watcher")

      registerListener { p =>
        log.debug(s"Event on: ${p.toString}")
        pollCB.invoke()
      }
    }

    override def postStop(): Unit = {
      log.debug("Stopping stage")
      offset.close
      reader.close
      segReader.close
      super.postStop()
    }
  }
}
