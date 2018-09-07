package swalka.reader

import swalka.offset.Offset
import swalka.reader.CommitableReader.CommitableResult

class CommitableReader[R <: Reader.Result](reader: Reader[R], offset: Offset) extends Reader[CommitableResult] {
  override def next: CommitableResult = {
    val res: Reader.Result = reader.next
    CommitableResult(() => offset.commit(res.offset), res)
  }

  override def hasNext: Boolean = reader.hasNext

  override def close(): Unit = {}
}

object CommitableReader {
  case class CommitableResult(commit: () => Unit,  rr: Reader.Result) extends Reader.Result {
    override val offset: Offset.Current = rr.offset
    override val data: Array[Byte] = rr.data
  }
}