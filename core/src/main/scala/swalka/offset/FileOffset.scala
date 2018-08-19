package swalka.offset

import java.nio.ByteBuffer
import java.nio.file.{Files, Path, StandardOpenOption}

import swalka._

class FileOffset(path: Path) extends Offset {

  private val offset = path.resolve("offset")
  private val fos = Files.newByteChannel(
    offset,
    StandardOpenOption.CREATE,
    StandardOpenOption.READ,
    StandardOpenOption.WRITE
  )

  override def commit(pos: Long): Unit = {
    fos.position(0)
    val ll = ByteBuffer.allocate(longSize)
    ll.putLong(pos)
    ll.flip()
    fos.write(ll)
  }

  override def current: Long = {
    if (fos.size() == 0) 0L
    else {
      val buf = ByteBuffer.allocate(longSize)
      fos.position(0)
      fos.read(buf)
      buf.flip()
      buf.getLong()
    }
  }

  def close: Unit = {
    fos.close()
  }

}
