package com.ruskulis.scalawal.writer

import java.io.FileOutputStream
import java.nio.ByteBuffer

import cats.Id

class FileWriter extends Writer[Id] {
  private val fos = new FileOutputStream("../journal.log", true)
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
