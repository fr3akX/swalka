package com.ruskulis.scalawal

import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Path
import java.time.Instant

import scala.annotation.tailrec

case class Segment(num: Int, closedAt: Option[Instant])

object Segment {

  def path(root: Path): Path = root.resolve("segments")

  val segmentSize = intSize + longSize

  def fromChannel(bc: SeekableByteChannel): Segment = {
    val bb = ByteBuffer.allocate(intSize)
    bc.read(bb)
    bb.flip()

    val segmentNum = bb.getInt()

    val hasFullSegment = bc.size() - bc.position() >= longSize
    val segmentClosedAt: Option[Instant] = (if (hasFullSegment) {
      val bl = ByteBuffer.allocate(longSize)
      bc.read(bl)
      bl.flip()
      Some(bl.getLong())
    } else {
      None
    }).map(Instant.ofEpochMilli)

    Segment(segmentNum, segmentClosedAt)
  }

  def closeSegment(segments: List[Segment], bc: SeekableByteChannel): List[Segment] = {
    val lastSeg :: ll = segments

    val bl = ByteBuffer.allocate(longSize)
    val closedAt = Instant.now()
    bl.putLong(closedAt.toEpochMilli)
    bl.flip()
    bc.write(bl)

    lastSeg.copy(closedAt = Some(closedAt)) :: ll
  }

  def newSegment(segments: List[Segment], bc: SeekableByteChannel): List[Segment] = {

    val nextSeg = segments match {
      case lastSeg :: _ => Segment(lastSeg.num+1, None)
      case Nil => Segment(0, None)
    }

    val bl = ByteBuffer.allocate(intSize)
    bl.putInt(nextSeg.num)
    bl.flip()
    bc.write(bl)

    nextSeg :: segments
  }

  def currentSegment(segments: List[Segment], bc: SeekableByteChannel): (Segment, List[Segment]) = segments match {
    case current :: _ if current.closedAt.isEmpty => (current, segments)
    case other =>
      val current :: rest = newSegment(other, bc)
      (current, current :: rest)
  }

  def hasNext(bc: SeekableByteChannel): Boolean = bc.position() < bc.size()

  def readAll(bc: SeekableByteChannel): List[Segment] = {
    @tailrec
    def recRead(bc: SeekableByteChannel, out: List[Segment]): List[Segment] = {
      val seg = fromChannel(bc)
      val rl = seg :: out
      if(seg.closedAt.isEmpty || !hasNext(bc)) rl
      else recRead(bc, rl)
    }

    if(bc.position() == bc.size()) List.empty[Segment]
    else recRead(bc, List.empty[Segment])
  }
}
