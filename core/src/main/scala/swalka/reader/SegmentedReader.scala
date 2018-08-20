package swalka.reader

import java.nio.file.{Files, Path, StandardOpenOption}

import com.typesafe.scalalogging.StrictLogging
import swalka.Segment
import swalka.offset.Offset.Current

class SegmentedReader(path: Path, readFrom: Current) extends Reader[Reader.Result] with StrictLogging {
  private val segmentReadChannel = Files.newByteChannel(Segment.path(path), StandardOpenOption.READ)

  private var currentSegment :: rest = Segment
    .readAll(segmentReadChannel)
    .takeWhile(_.num >= readFrom.segment).reverse

  private var currentLog = new FileReader(path, currentSegment.num, readFrom.pos)


  private def openNext = rest match {
    case Nil =>
      logger.debug("No next log, reached oef")
    case h :: t =>
      logger.debug(s"Found next log $h $t")
      currentSegment = h
      rest = t
      currentLog = new FileReader(path, currentSegment.num, 0)
  }

  override def next: Reader.ReadResult = currentLog.next

  override def hasNext: Boolean = {
    val hn = currentLog.hasNext
    if(hn) hn
    else {
      logger.debug(s"openining next log, from current: $currentSegment")
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
  val Beginning = Current(0, 0)
}
