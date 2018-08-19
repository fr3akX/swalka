package swalka.reader

class ReaderIterator(reader: Reader) extends Iterator[Reader#R] {
  override def hasNext: Boolean = reader.hasNext

  override def next(): Reader#R = reader.next

  def close(): Unit = reader.close
}
