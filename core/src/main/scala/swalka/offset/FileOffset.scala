package swalka.offset

import java.nio.ByteBuffer
import java.nio.file.{Files, Path, StandardOpenOption}

import swalka._
import swalka.offset.Offset.Current

class FileOffset(path: Path) extends Offset {

  private val offset = path.resolve("offset")
  private val fos = Files.newByteChannel(
    offset,
    StandardOpenOption.CREATE,
    StandardOpenOption.READ,
    StandardOpenOption.WRITE
  )

  override def commit(offset: Current): Unit = {
    fos.position(0)
    val ll = ByteBuffer.allocate(intSize + longSize)
    ll.putInt(offset.segment)
    ll.putLong(offset.pos)
    ll.flip()
    fos.write(ll)
  }

  override def current: Current = {
    if (fos.size() == 0) Current(0, 0L)
    else {
      val buf = ByteBuffer.allocate(intSize + longSize)
      fos.position(0)
      fos.read(buf)
      buf.flip()
      Current(buf.getInt(), buf.getLong())
    }
  }

  def close: Unit = {
    fos.close()
  }

}
