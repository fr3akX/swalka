package swalka.writer

import java.nio.ByteBuffer
import java.nio.file.{Files, Path, StandardOpenOption}

import com.typesafe.scalalogging.LazyLogging
import swalka.Segment

import scala.concurrent.duration.FiniteDuration


class SegmentedWriter(
  path: Path,
  maxBytesPerSegment: Long,
  maxClosedSegmentAge: FiniteDuration) extends Writer with LazyLogging {

  logger.debug(s"Opening segments: ${ Segment.path(path).toAbsolutePath.toString }")

  private val c = Files.newByteChannel(Segment.path(path), StandardOpenOption.DSYNC, StandardOpenOption.SYNC, StandardOpenOption.READ,
    StandardOpenOption.WRITE, StandardOpenOption.CREATE)
  //private val fos = new FileOutputStream(path.resolve("segments").toAbsolutePath.toString, true)
  //private val c = fos.getChannel

  private var (currentSegment, segments) = Segment.currentSegment(Segment.readAll(c), c)
  logger.info(s"Segments: $segments")

  private var writer = new FileWriter(path, currentSegment.num)
  private var size: Long = writer.size

  private def newSegment = {
    size = 0L
    writer.close

    val invalidated = Segment.invalidate(segments, maxClosedSegmentAge)
    if(invalidated != segments) {
      logger.info("Overwriting segments")
      Segment.rewrite(invalidated, c)
    }

    val currentSeg :: newSegments = Segment.newSegment(
      Segment.closeSegment(invalidated, c),
      c
    )

    currentSegment = currentSeg
    segments = currentSeg :: newSegments

    logger.info(s"Segments: $segments")
    writer = new FileWriter(path, currentSegment.num)
    flush
  }

  @inline
  override def write(data: ByteBuffer): Unit = {
    if (size >= maxBytesPerSegment) {
      logger.info("Opening new segment")
      newSegment
    }
    writer.write(data)
    size += data.capacity()
  }

  override def flush: Unit = {
    writer.flush
    //    c.force(true)
    //    fos.getFD.sync()
  }

  override def close: Unit = {
    c.close()
    //    fos.close()
    writer.close
  }
}