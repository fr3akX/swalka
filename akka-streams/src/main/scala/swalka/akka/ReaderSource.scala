package swalka.akka

import akka.stream.scaladsl.Source
import swalka.reader.ReaderIterator

object ReaderSource {
  def sourceFromReaderIterator(reader: => ReaderIterator) =
    Source.fromIterator(() => reader)
}
