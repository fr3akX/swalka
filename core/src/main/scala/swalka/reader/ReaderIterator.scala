package swalka.reader

class ReaderIterator[O <: Reader.Result](reader: Reader[O]) extends Iterator[O] {
    override def hasNext: Boolean = reader.hasNext

    override def next(): O = reader.next

    def close(): Unit = reader.close
}
