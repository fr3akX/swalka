package swalka.streams

import java.nio.ByteBuffer

import akka.stream.javadsl.SourceQueueWithComplete
import swalka.streams.WatchedReaderFlow.Notification
import swalka.writer.Writer

class WriterNotifier(queue: SourceQueueWithComplete[Notification], writer: Writer) extends Writer {
  override def write(data: ByteBuffer): Notification = {
    writer.write(data)
    queue.offer(())
  }
  override def flush(): Unit = writer.flush()
  override def close(): Unit = writer.close()
}

object WriterNotifier {
  def apply(queue: SourceQueueWithComplete[Notification], writer: Writer): WriterNotifier =
    new WriterNotifier(queue, writer)
}