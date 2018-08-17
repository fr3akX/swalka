package com.ruskulis.scalawal.writer

import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.{Path, Paths}

import cats.Id


class FileWriter(path: Path, segment: Int) extends Writer[Id] {

  private val fos = new FileOutputStream(Paths.get(path.toString, s"journal.$segment").toAbsolutePath.toString, true)
  private val c = fos.getChannel

  override def write(data: ByteBuffer): Unit = {
    data.flip()
    c.write(data)
  }

  override def flush: Unit = {
    c.force(true)
    fos.getFD.sync()
  }
  override def close: Id[Unit] = {
    c.close()
    fos.close()
  }
}
