package swalka.writer

import java.io.Closeable
import java.nio.channels.SeekableByteChannel
import java.nio.file.{Files, Path, StandardOpenOption}
import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import com.typesafe.scalalogging.StrictLogging
import swalka.util.Resource
import swalka.{Journal, Segment}

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

class HouseKeeper(dbPath: Path, ec: ScheduledExecutorService, interval: FiniteDuration) extends StrictLogging {
  private def runCleanup() = {
    logger.debug("Running")
    Resource[SeekableByteChannel, Unit](
      () => Files.newByteChannel(Segment.path(dbPath), StandardOpenOption.READ),
      _.close()
    ) { segmentReadChannel =>
      val validSegments = Segment.readAll(segmentReadChannel).map(_.num).toSet

      val onFsSegments = Files.list(dbPath).iterator().asScala
        .map(_.getFileName)
        .filter(_.toString.startsWith(Journal.JournalFilePrefix))
        .toList
        .map(_.toString)
        .map(_.split("\\.").last.toInt).toSet

      val removeSegments = onFsSegments -- validSegments
      if (removeSegments.nonEmpty) logger.info(s"Removing segments: ${ removeSegments.toList.sorted.mkString(", ") }")
      removeSegments foreach { s => Files.delete(dbPath.resolve(Journal.segment(s))) }
    }.run()
    logger.debug("Finishing")
  }

  def start(): Closeable = {
    logger.info("Starting...")
    val sched = ec.scheduleWithFixedDelay(
      () => runCleanup(), 0, interval.toSeconds, TimeUnit.SECONDS)

    () => {
      logger.info("Stopping...")
      sched.cancel(false)
    }
  }
}
