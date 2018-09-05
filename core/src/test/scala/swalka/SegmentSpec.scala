package swalka

import java.nio.channels.SeekableByteChannel
import java.nio.file.{Files, StandardOpenOption}
import java.time.Instant

import org.scalatest.{Assertion, FlatSpec, Matchers}

import scala.util.Try
import scala.concurrent.duration._

class SegmentSpec extends FlatSpec with Matchers {


  def withByteChannel[A](f: SeekableByteChannel => A): A = {
    val xxx = Files.createTempFile("testfile", "test")
    val bc = Files.newByteChannel(xxx.toAbsolutePath, StandardOpenOption.WRITE, StandardOpenOption.READ)
    val r = Try(f(bc))
    bc.close()
    r.get
  }

  def withRecoveryTest[A](f: SeekableByteChannel => List[Segment]): Assertion = {
    val xxx = Files.createTempFile("testfile", "test")
    val bc = Files.newByteChannel(xxx.toAbsolutePath, StandardOpenOption.WRITE, StandardOpenOption.READ)

    val r = f(bc)
    bc.close()

    val bc2 = Files.newByteChannel(xxx.toAbsolutePath, StandardOpenOption.WRITE, StandardOpenOption.READ)
    Segment.readAll(bc2) shouldBe r
  }

  it should "be able to read empty segments" in withByteChannel { bc =>
    val segments = Segment.readAll(bc)
    segments shouldBe List.empty[Segment]
  }

  it should "be able to create first segment" in withRecoveryTest { bc =>
    val segments = Segment.readAll(bc)
    val newSegments = Segment.newSegment(segments, bc)
    segments.length should be.<(newSegments.length)
    newSegments
  }

  it should "be able recover from data file" in withRecoveryTest { bc =>
      val segments = Segment.readAll(bc)
      Segment.newSegment(segments, bc)
  }

  it should "be able to close segment" in withRecoveryTest { bc =>
    val segments = Segment.readAll(bc)
    val newSegments = Segment.newSegment(segments, bc)
    val closed :: rest = Segment.closeSegment(newSegments, bc)
    closed.closedAt shouldBe defined
    closed :: rest
  }

  it should "be able to invalidate segments" in {
    val segments = List(
      Segment(0, Some(Instant.now().minusSeconds(300))),
      Segment(1, Some(Instant.now().minusSeconds(200))),
      Segment(2, None)
    )

    Segment.invalidate(segments, 250.seconds) shouldBe segments.tail
  }

  it should "be able to rewrite segmets" in withRecoveryTest { bc =>
    val segments = Segment.readAll(bc)
    Segment.rewrite(segments, bc)
    segments
  }

}
