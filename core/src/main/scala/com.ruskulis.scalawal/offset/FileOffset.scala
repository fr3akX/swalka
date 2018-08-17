package com.ruskulis.scalawal.offset

import java.nio.ByteBuffer
import java.nio.file.{Files, Paths, StandardOpenOption}

import cats.Id
import com.ruskulis.scalawal._

class FileOffset extends Offset[Id] {

  private val offsetFile = "offset"
  private val fos = Files.newByteChannel(
    Paths.get(offsetFile),
    StandardOpenOption.CREATE,
    StandardOpenOption.READ,
    StandardOpenOption.WRITE
  )

  override def commit(pos: Long): Id[Unit] = {
    fos.position(0)
    val ll =  ByteBuffer.allocate(longSize)
    ll.putLong(pos)
    ll.flip()
    fos.write(ll)
  }

  override def current: Id[Long] = {
    if(fos.size() == 0) 0L
    else {
      val buf = ByteBuffer.allocate(longSize)
      fos.position(0)
      fos.read(buf)
      buf.flip()
      buf.getLong()
    }
  }

  def close: Id[Unit] = {
    fos.close()
  }

}
