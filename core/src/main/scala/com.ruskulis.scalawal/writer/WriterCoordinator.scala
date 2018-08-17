package com.ruskulis.scalawal.writer

import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.{Path, Paths}

import cats.Id


class WriterCoordinator(path: Path, maxBytesPerSegment: Long) extends Writer[Id] {

  private val fos = new FileOutputStream(Paths.get(path.toString, s"segments").toAbsolutePath.toString, true)
  private val c = fos.getChannel

  var (currentSegment, segments) = Segment.currentSegment(Segment.readAll(c), c)
  var writer = new FileWriter(path, currentSegment.num)

  def newSegment = {
    writer.close
    val (currentSeg :: newSegments) = Segment.newSegment(
      Segment.closeSegment(segments, c),
      c
    )

    currentSegment = currentSeg
    segments = currentSeg :: newSegments

    writer = new FileWriter(path, currentSegment.num)
    flush
  }

  override def write(data: ByteBuffer): Id[Unit] = {
    if(writer.size >= maxBytesPerSegment) newSegment
    writer.write(data)
  }

  override def flush: Id[Unit] = {
    writer.flush
    c.force(true)
    fos.getFD.sync()
  }

  override def close: Id[Unit] = {
    c.close()
    fos.close()
    writer.close
  }
}