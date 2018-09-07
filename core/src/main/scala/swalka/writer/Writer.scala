package swalka.writer

import java.nio.ByteBuffer

import scala.language.higherKinds

trait Writer {
  def write(data: ByteBuffer): Unit

  def flush(): Unit //intended for fsync
  def close(): Unit
}
