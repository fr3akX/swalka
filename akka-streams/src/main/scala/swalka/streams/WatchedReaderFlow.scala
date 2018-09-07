package swalka.streams

import java.nio.file.Path

import akka.stream._
import akka.stream.stage._
import swalka.offset.FileOffset
import swalka.reader.CommitableReader.CommitableResult
import swalka.reader.{CommitableReader, SegmentedReader}
import WatchedReaderFlow._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

/**
  * TODO logic: do not close file on eof until it has been marked as closed in segments
  * @param readerId
  * @param dbPath
  */
class WatchedReaderFlow(readerId: String, dbPath: Path) extends GraphStage[FlowShape[Notification, CommitableResult]] {

  val out: Outlet[CommitableResult] = Outlet("CommitableResultSource.out")
  val in: Inlet[Notification] = Inlet("WriterNotifications.in")

  override def shape: FlowShape[Notification, CommitableResult] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with StageLogging {

    var offset = new FileOffset(readerId, dbPath)
    var segReader = new SegmentedReader(dbPath, offset.current)
    var reader = new CommitableReader(segReader, offset)

    var outstanding = false
    var downStreamDemands: Boolean = false

    override def preStart(): Unit = {
      super.preStart()
      pull(in)
    }

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        log.debug("onPull")
        if(reader.hasNext) {
          log.debug("pushing out")
          push(out, reader.next)
        } else {
          log.debug("reached oef while onPull")
          downStreamDemands = true
        }
      }
    })

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        grab(in)
        pull(in)
        log.debug("Got push")
        if(downStreamDemands) {
          log.debug(s"has downStreamDemands $downStreamDemands")
          reloadOnEof()
        } else outstanding = true
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
      } else {
        log.debug("not reloading as has next")
      }

      if(reader.hasNext && isAvailable(out)) {
        log.debug("Emiting output")
        downStreamDemands = false
        push(out, reader.next)
      } else {
        log.debug("reopened log, but nothing to read")
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

object WatchedReaderFlow {
  type Notification = Unit

  def apply(readerId: String, dbPath: Path): Flow[Notification, CommitableResult, _] =
    Flow.fromGraph(new WatchedReaderFlow(readerId, dbPath))

  def notifiableProcessor(readerFlow: Flow[Notification, CommitableResult, _], processor: Sink[CommitableResult, _]) =
    Source
      .queue[Notification](1, OverflowStrategy.dropNew)
      .via(readerFlow)
      .toMat(processor)(Keep.left)
}