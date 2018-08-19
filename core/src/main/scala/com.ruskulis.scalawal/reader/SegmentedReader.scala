package com.ruskulis.scalawal.reader

import java.nio.file.{Files, Path, StandardOpenOption}

import com.ruskulis.scalawal.Segment
import com.ruskulis.scalawal.reader.SegmentedReader.ReadFrom

class SegmentedReader(path: Path, readFrom: ReadFrom) extends Reader {
  private val segmentReadChannel = Files.newByteChannel(Segment.path(path), StandardOpenOption.READ)

  var currentSegment :: rest = Segment
    .readAll(segmentReadChannel)
    .takeWhile(_.num >= readFrom.segment).reverse

  var currentLog = new FileReader(path, currentSegment.num, 0)


  private def openNext = rest match {
    case Nil =>

    case h :: t =>
      currentSegment = h
      rest = t
      currentLog = new FileReader(path, currentSegment.num, 0)
  }

  override def next: Reader.ReadResult = currentLog.next

  override def hasNext: Boolean = {
    val hn = currentLog.hasNext
    if(hn) hn
    else {
      openNext
      currentLog.hasNext
    }
  }

  override def close: Unit = {
    segmentReadChannel.close()
    currentLog.close
  }
}

object SegmentedReader {
  val Beginning = ReadFrom(0, 0)
  case class ReadFrom(segment: Int, offset: Long)
}
