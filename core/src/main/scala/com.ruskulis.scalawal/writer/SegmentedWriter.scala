package com.ruskulis.scalawal.writer

import java.nio.ByteBuffer
import java.nio.file.{Files, Path, StandardOpenOption}

import cats.Id
import com.ruskulis.scalawal.Segment


class SegmentedWriter(path: Path, maxBytesPerSegment: Long) extends Writer[Id] {

  println(s"Opening segments: ${Segment.path(path).toAbsolutePath.toString}")
  private val c = Files.newByteChannel(Segment.path(path), StandardOpenOption.DSYNC, StandardOpenOption.SYNC,StandardOpenOption.READ,
    StandardOpenOption.WRITE, StandardOpenOption.CREATE)
  //private val fos = new FileOutputStream(path.resolve("segments").toAbsolutePath.toString, true)
  //private val c = fos.getChannel

  private var (currentSegment, segments) = {
    val allsegs = Segment.readAll(c)
    Segment.currentSegment(allsegs, c)
  }

  private var writer = new FileWriter(path, currentSegment.num)
  private var size: Long = writer.size

  private def newSegment = {
    size = 0L
    writer.close
    val currentSeg :: newSegments = Segment.newSegment(
      Segment.closeSegment(segments, c),
      c
    )

    currentSegment = currentSeg
    segments = currentSeg :: newSegments

    writer = new FileWriter(path, currentSegment.num)
    flush
  }

  @inline
  override def write(data: ByteBuffer): Id[Unit] = {
    if(size >= maxBytesPerSegment) newSegment
    writer.write(data)
    size += data.capacity()
  }

  override def flush: Id[Unit] = {
    writer.flush
//    c.force(true)
//    fos.getFD.sync()
  }

  override def close: Id[Unit] = {
    c.close()
//    fos.close()
    writer.close
  }
}