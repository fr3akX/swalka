package swalka.reader

import swalka.reader.Reader.ReadResult

class ReaderIterator(reader: Reader) extends Iterator[ReadResult] {
  override def hasNext: Boolean = reader.hasNext

  override def next(): ReadResult = reader.next

  def close(): Unit = reader.close
}
