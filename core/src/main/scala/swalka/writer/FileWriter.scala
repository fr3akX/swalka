package swalka.writer

import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.{Path, Paths}

import swalka.Journal

class FileWriter(path: Path, segment: Int) extends Writer {

  private val fos = new FileOutputStream(Paths.get(path.toString, Journal.segment(segment)).toAbsolutePath.toString, true)
  private val c = fos.getChannel

  override def write(data: ByteBuffer): Unit = {
    data.flip()
    c.write(data)
  }

  override def flush: Unit = {
    c.force(true)
    fos.getFD.sync()
  }
  override def close: Unit = {
    c.close()
    fos.close()
  }

  def size: Long = c.size()
}
