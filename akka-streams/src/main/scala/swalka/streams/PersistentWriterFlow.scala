package swalka.streams

import akka.stream._
import akka.stream.stage._
import swalka.Record
import swalka.writer.Writer

class PersistentWriterFlow(writer: => Writer)(implicit mat: Materializer) extends GraphStage[FlowShape[Record, Unit]] {

  private val in = Inlet[Record]("Record.In")
  private val out = Outlet[Unit]("WriterNotification.Out")

  override def shape: FlowShape[Record, Unit] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with StageLogging {

    var outstanding = false
    var demand = false

    val w = writer

    override def postStop(): Unit = {
      w.close
      super.postStop()
    }

    override def preStart(): Unit = {
      super.preStart()
      pull(in)
      log.info("Starting writer")
    }

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        log.debug("Writing...")
        val elem = grab(in)
        w.write(Record.toByteBuffer(elem))
        pull(in)

        if (isAvailable(out)) {
          log.debug("Pushing out")
          push(out, ())
        } else {
          outstanding = true
          log.debug("no demand from upstream")
        }
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        log.debug(s"On pull outstanding=$outstanding")
        if (outstanding && demand) {
          push(out, ())
          outstanding = false
          demand = false
        } else {
          log.debug("Upstream demand")
          demand = true
        }
      }
    })
  }
}
