package com.ruskulis.scalawal.reader

import java.io.FileInputStream

import cats.Id
import com.ruskulis.scalawal.Record
import com.ruskulis.scalawal.reader.Reader.ReadResult

class FileReader(initalOffset: Long) extends Reader[Id] {

  private val in = new FileInputStream("journal.log")
  private val channel = in.getChannel

  private var offset: Long = initalOffset
  in.skip(initalOffset)
  in.getChannel.position()

  override def next: Id[Reader.ReadResult] = {
    val (totalLength, data) = Record.fromChannel(channel)
    offset += totalLength
    ReadResult(offset, data.data)
  }

  def hasNext: Id[Boolean] = in.available() > 0

  override def close: Id[Unit] = in.close()
}
