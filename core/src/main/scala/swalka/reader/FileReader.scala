package swalka.reader

import java.io.FileInputStream
import java.nio.file.Path

import swalka.Record
import swalka.offset.Offset.Current
import swalka.reader.Reader.ReadResult

class FileReader(path: Path, segment: Int, initalOffset: Long) extends Reader[Reader.ReadResult] {

  private val in = new FileInputStream(path.resolve(s"journal.$segment").toAbsolutePath.toString)
  private val channel = in.getChannel

  private var offset: Long = initalOffset
  in.skip(initalOffset)

  override def next: Reader.ReadResult = {
    val (totalLength, data) = Record.fromChannel(channel)
    offset += totalLength
    ReadResult(Current(segment, offset), data.data)
  }

  def hasNext: Boolean = in.available() > 0

  override def close: Unit = in.close()
}
