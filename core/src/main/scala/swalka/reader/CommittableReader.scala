package swalka.reader

import swalka.offset.Offset
import swalka.reader.CommittableReader.CommitableResult

class CommittableReader(reader: Reader, offset: Offset) extends Reader {
  override type R = CommitableResult

  override def next: CommitableResult = {
    val res: Reader.Result = reader.next
    CommitableResult(() => offset.commit(res.offset), res)
  }

  override def hasNext: Boolean = reader.hasNext

  override def close: Unit = reader.close
}

object CommittableReader {
  case class CommitableResult(commit: () => Unit,  rr: Reader.Result) extends Reader.Result {
    override val offset: Offset.Current = rr.offset
    override val data: Array[Byte] = rr.data
  }
}