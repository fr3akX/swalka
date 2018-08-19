package swalka.streams

import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import swalka.Record
import swalka.writer.Writer

class WriterSinkStage(writer: => Writer) extends GraphStage[SinkShape[Record]]{

  private val in = Inlet[Record]("Record.In")

  override def shape: SinkShape[Record] = SinkShape.of(in)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    val w = writer

    override def postStop(): Unit = {
      w.close
      super.postStop()
    }

    override def preStart(): Unit = {
      super.preStart()
      pull(in)
    }

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val elem = grab(in)
        w.write(Record.toByteBuffer(elem))
        pull(in)
      }
    })
  }
}
